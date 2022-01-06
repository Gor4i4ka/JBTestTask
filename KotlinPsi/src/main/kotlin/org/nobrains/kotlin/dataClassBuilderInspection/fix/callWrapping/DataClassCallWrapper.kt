package org.nobrains.kotlin.dataClassBuilderInspection.fix.callWrapping

import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf
import org.nobrains.kotlin.dataClassBuilderInspection.utils.*

object DataClassCallWrapper {

    private val handledCollectionsOf = setOf("listOf", "setOf", "stackOf")

    fun wrapConstructorCall(call: KtCallElement): KtCallElement {

        val wrapperStringBuilder = StringBuilder()

        val resolvedConstructor = resolveConstructorOrNull(call)

        if (resolvedConstructor != null) {
            val resolvedConstructorName = resolvedConstructor.nameAsSafeName.toString()
            val resolvedConstructorParameters = resolvedConstructor.valueParameters
            val resolvedBuildFunction = findBuilderAndBuildForClass(resolvedConstructorName, call.project)?.first

            // Generating the wrapper

            wrapperStringBuilder
                .append("${resolvedBuildFunction?.nameAsSafeName}")
                .append(" {\n")
                .apply {
                    for (argumentIndex in call.valueArguments.indices) {
                        append(
                            processField(
                                resolvedConstructorParameters[argumentIndex],
                                call.valueArguments[argumentIndex] as KtValueArgument
                            )
                        )
                    }
                }
                .append("}")

            // Creating the expression and processing children

            val kotlinFactory = KtPsiFactory(call.project)
            val wrapperExpression = kotlinFactory.createExpression(wrapperStringBuilder.toString()) as KtCallElement
            visitChildren(wrapperExpression)
            return wrapperExpression
        } else {
            visitChildren(call)
            return call
        }
    }

    fun isApplicable(call: KtCallElement): Boolean {

        val potentialConstructor = resolveConstructorOrNull(call)

        if (potentialConstructor is KtPrimaryConstructor) {
            val clazz = potentialConstructor.parent as? KtClass ?: return false

            if (clazz.isData()) {
                val potentialBuildingPair = findBuilderAndBuildForClass(
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

    private fun processField(parameter: KtParameter, argument: KtValueArgument): String {

        val argumentExpression: KtCallElement? = argument.getArgumentExpression() as? KtCallElement

        // Data class primary constructor expressions will be handled by children, we handle collections here
        if (argumentExpression?.firstChild?.text in handledCollectionsOf) {

            // We found collection call, - now handling it
            // Check if collection of data class type with builder

            val genericType = parameter.type()?.extractCollectionArgumentName()
            val potentialBuilderPair = findBuilderAndBuildForClass(genericType, parameter.project)
            val potentialBuilder = potentialBuilderPair?.second

            // We are safe if building function is found
            // WARNING: potentialBuilder.findFunctionByName("${parameter.nameAsSafeName}Element") != null not works? BUG
            if (potentialBuilder != null
                && resolveFunctionOrNull("${parameter.nameAsSafeName}Element", potentialBuilder.project) != null
            ) {
                val collectionWrapper = StringBuilder("\n")
                val collectionArgs = argumentExpression?.valueArguments

                if (collectionArgs != null) {
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
                }
                return collectionWrapper.toString()
            }

            var resultCollectionInvocation = argument.getArgumentExpression()?.text
            resultCollectionInvocation =
                "mutable${resultCollectionInvocation?.get(0)?.uppercase()}${resultCollectionInvocation?.drop(1)}\n"
            return "${parameter.nameAsSafeName} = $resultCollectionInvocation"
        }

        return "${parameter.nameAsSafeName} = ${argument.getArgumentExpression()?.text}\n"
    }

    private fun visitChildren(parent: KtElement) {
        for (child in parent.children) {
            if (child is KtCallElement && isApplicable(child))
                child.replace(wrapConstructorCall(child))
            visitChildren(child as KtElement)
        }
    }

}

