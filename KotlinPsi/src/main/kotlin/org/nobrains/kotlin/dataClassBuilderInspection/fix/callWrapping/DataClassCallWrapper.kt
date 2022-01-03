package org.nobrains.kotlin.dataClassBuilderInspection.fix.callWrapping

import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.findFunctionByName
import org.nobrains.kotlin.dataClassBuilderInspection.utils.findBuilderAndBuildForClass
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveConstructorOrNull

object DataClassCallWrapper {

    fun wrapConstructorCall(call: KtCallElement) {

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

    private fun applyTo(call: KtCallElement): KtCallElement {

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

    private fun processField(parameter: KtParameter, argument: KtValueArgument): String {

        val argumentExpression: KtCallElement? = argument.getArgumentExpression() as? KtCallElement

        // Data class primary constructor expressions will be handled by children, we handle collections here
        if (argumentExpression?.firstChild?.text in handledCollectionsOf) {

            // We found collection call, - now handling it
            // Check if collection of data class type with builder
            val parameterTypeString = parameter.type().toString()
            val genericType = Regex("<.*>").find(parameterTypeString)?.value?.drop(1)?.dropLast(1)

            val potentialBuilderPair = findBuilderAndBuildForClass(genericType, parameter.project)
            val potentialBuilder = potentialBuilderPair?.second as? KtClass

            // We are safe if building function is found
            if (potentialBuilder != null
                && potentialBuilder.findFunctionByName("${parameter.nameAsSafeName}Element") != null
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
                child.replace(applyTo(child))
            visitChildren(child as KtElement)
        }
    }

}