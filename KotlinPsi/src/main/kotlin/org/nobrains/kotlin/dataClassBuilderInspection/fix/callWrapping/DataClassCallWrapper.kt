package org.nobrains.kotlin.dataClassBuilderInspection.fix.callWrapping

import org.jetbrains.kotlin.idea.debugger.sequence.psi.callName
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf
import org.nobrains.kotlin.dataClassBuilderInspection.utils.*

object DataClassCallWrapper {

    private val handledCollectionsOf = setOf("listOf", "setOf", "stackOf")

    fun wrapConstructorCall(call: KtCallExpression): KtCallExpression {

        val wrapperStringBuilder = StringBuilder()

        val resolvedConstructor = resolveIndexConstructorOrNull(call)
//        val resolvedConstructor = resolveReferenceConstructorOrNull(call)

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
                        append(
                            processField(
                                resolvedConstructorParameters[argumentIndex],
                                call.valueArguments[argumentIndex]
                            )
                        )
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

    fun isApplicable(call: KtCallExpression): Boolean {

        val potentialConstructor = resolveIndexConstructorOrNull(call)
//        val potentialConstructor = resolveReferenceConstructorOrNull(call)

        if (potentialConstructor is KtPrimaryConstructor) {
            val clazz = potentialConstructor.parent as? KtClass ?: return false

            if (clazz.isData()) {
                val potentialBuildingPair = findIndexBuilderAndBuildForClass(
                    clazz.nameAsSafeName.toString(),
                    clazz.project
                )
//                val potentialBuildingPair = findReferenceBuilderAndBuildForClass(clazz)
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

    private fun processField(parameter: KtParameter, argument: KtValueArgument): String {

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
            // WARNING: potentialBuilder.findFunctionByName("${parameter.nameAsSafeName}Element") != null not works? BUG
            if (potentialBuilder != null
                && resolveIndexFunctionOrNull("${parameter.nameAsSafeName}Element", potentialBuilder.project) != null
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

        // Default field processing
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

