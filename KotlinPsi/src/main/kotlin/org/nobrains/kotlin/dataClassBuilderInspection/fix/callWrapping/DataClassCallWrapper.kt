package org.nobrains.kotlin.dataClassBuilderInspection.fix.callWrapping

import org.jetbrains.kotlin.idea.debugger.sequence.psi.callName
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf
import org.nobrains.kotlin.dataClassBuilderInspection.utils.*

object DataClassCallWrapper {

    private val handledCollectionsOf = setOf("listOf", "setOf", "stackOf")

    fun wrapConstructorCall(call: KtCallExpression): KtCallExpression {

        val wrapperStringBuilder = StringBuilder()

        val resolvedConstructor = resolveIndexConstructorWithBuilderOrNull(call)

        if (resolvedConstructor != null) {
            val resolvedConstructorName = resolvedConstructor.nameAsSafeName.toString()
            val resolvedConstructorParameters = resolvedConstructor.valueParameters
            val resolvedBuildFunction = findIndexBuilderAndBuildForClass(resolvedConstructorName, call.project)?.first

            // Generating the wrapper

            wrapperStringBuilder
                .append("${resolvedBuildFunction?.nameAsSafeName}")
                .append(" {\n")
                .apply {
                    for (argumentIndex in call.valueArguments.indices) {

                        val argument = call.valueArguments[argumentIndex]
                        var correspondingParameter: KtParameter?

                        // Processing Named arguments
                        if (argument.isNamed()) {
                            correspondingParameter = argument.getArgumentName()?.text?.let {
                                findParameterByName(
                                    it,
                                    resolvedConstructorParameters
                                )
                            }

                            append(correspondingParameter?.let {
                                processField(
                                    it,
                                    argument,
                                    resolvedConstructor.containingKtFile
                                )
                            }
                            )
                        }

                        // Processing arguments by default
                        else {
                            correspondingParameter = resolvedConstructorParameters[argumentIndex]
                            append(
                                processField(
                                    correspondingParameter,
                                    argument,
                                    resolvedConstructor.containingKtFile
                                )
                            )
                        }
                    }
                }
                .append("}")

            // Creating the expression and processing children

            val kotlinFactory = KtPsiFactory(call.project)
            val wrapperExpression = kotlinFactory.createExpression(wrapperStringBuilder.toString()) as KtCallExpression
            visitChildren(wrapperExpression)
            return wrapperExpression
        } else {
            visitChildren(call)
            return call
        }
    }

    private fun isApplicable(call: KtCallExpression): Boolean {

        val potentialConstructor = resolveIndexConstructorWithBuilderOrNull(call)

        if (potentialConstructor is KtPrimaryConstructor) {
            val clazz = potentialConstructor.parent as? KtClass ?: return false

            if (clazz.isData()) {
                val potentialBuildingPair = findIndexBuilderAndBuildForClass(
                    clazz.nameAsSafeName.toString(),
                    clazz.project
                )
                if (potentialBuildingPair != null) {

                    // Sanity check
                    if (call.isInsideOf(listOf(potentialBuildingPair.second))
                    )
                        return false
                }
                return true
            }
        }
        return false
    }

    private fun processField(
        parameter: KtParameter,
        argument: KtValueArgument,
        fileWithConstructorDeclaration: KtFile
    ): String {

        //val argumentExpression = argument.getArgumentExpression() as? KtCallExpression
        val argumentExpression = argument.getArgumentExpression()

        // Field processing if "CollectionOf" call
        // Data class primary constructor expressions will be handled by children, we handle collections here
        if (argumentExpression is KtCallExpression &&
            argumentExpression.callName() in handledCollectionsOf
        ) {

            // We found collection call, - now handling it
            // Check if collection of data class type with builder

            val genericType = parameter.type()?.extractCollectionArgumentNameOrNull()
            val potentialBuilderPair = findIndexBuilderAndBuildForClass(genericType, parameter.project)
            val potentialBuilder = potentialBuilderPair?.second

            // We are safe if building function is found
            if (potentialBuilder != null
                && functionPresentInFile("${parameter.nameAsSafeName}Element", fileWithConstructorDeclaration)
            ) {
                val collectionWrapper = StringBuilder("\n")
                val collectionArgs = argumentExpression.valueArguments

                for (collectionArgument in collectionArgs) {
                    val collectionArgumentWrapper = StringBuilder()
                        .append("${parameter.nameAsSafeName}Element ")
                        .append("(\n")
                        .apply {
                            append(collectionArgument.getArgumentExpression()?.text)
                        }
                        .append("\n)\n")

                    collectionWrapper.append(collectionArgumentWrapper)
                }

                return collectionWrapper.toString()
            }

            // Getting info about the function call as String and transforming it
            val mutableInstantiation = argumentExpression.text?.toMutableInstantiationByName()
            return "${parameter.nameAsSafeName} = $mutableInstantiation"
        }

        // Field processing by default
        return "${parameter.nameAsSafeName} = ${argumentExpression?.text}\n"
    }

    private fun visitChildren(parent: KtElement) {
        for (child in parent.children) {
            if (child is KtCallExpression && isApplicable(child))
                child.replace(wrapConstructorCall(child))
            visitChildren(child as KtElement)
        }
    }

}

