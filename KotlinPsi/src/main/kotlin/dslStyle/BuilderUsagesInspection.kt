package dslStyle

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.fir.resolve.dfa.stackOf
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.findFunctionByName
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf

class BuilderUsagesInspection :
    AbstractApplicabilityBasedInspection<KtNameReferenceExpression>(KtNameReferenceExpression::class.java) {

    private lateinit var kotlinFactory: KtPsiFactory
    private lateinit var project: Project

    private var debugFlag: Boolean? = null
    private val handledCollectionsOf = setOf<String>("listOf", "setOf", "stackOf")

    override val defaultFixText: String
        get() = "Wrap this data class usage in a DSL-style builder"

    override fun inspectionText(element: KtNameReferenceExpression): String {
        return "Generates DSL-builders for the data class and wraps this usage"
    }

    override fun applyTo(element: KtNameReferenceExpression, project: Project, editor: Editor?) {
        kotlinFactory = KtPsiFactory(project)
        this.project = element.project

        // Firstly Generate all builders
        BuilderGenerationSubInspection(element.parent as KtCallElement, project)
            .launchBuilderGeneration()

        element.parent.replace(applyTo(element.parent as KtCallElement))
    }

    private fun applyTo(call: KtCallElement): KtCallElement {

        val wrapperStringBuilder = StringBuilder()

        val resolvedConstructor = resolveConstructor(call)

        if (resolvedConstructor != null) {
            val resolvedConstructorName = resolvedConstructor.nameAsSafeName.toString()
            val resolvedConstructorParameters = resolvedConstructor.valueParameters

            val resolvedBuildFunction = findBuildAndBuilderByClassName(resolvedConstructorName)?.first!!

            // Generating the wrapper

            wrapperStringBuilder
                .append("${resolvedBuildFunction.nameAsSafeName}")
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

            val potentialBuilderPair = findBuildAndBuilderByClassName(genericType)
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

    override fun isApplicable(element: KtNameReferenceExpression): Boolean {

        this.project = element.project

        val parent = element.parent
        if (parent is KtCallElement && element.text !in handledCollectionsOf)
            return isApplicable(parent)
        return false
    }

    private fun isApplicable(call: KtCallElement): Boolean {
        if (debugFlag != null)
            println("${call.firstChild?.text}")

        val potentialConstructor = resolveConstructor(call)

        if (potentialConstructor is KtPrimaryConstructor) {
            val clazz = potentialConstructor.parent as KtClass
            if (clazz.isData()) {
                val potentialBuildingPair = findBuildAndBuilderByClassName(
                    clazz.nameAsSafeName.toString()
                )
                if (potentialBuildingPair != null) {

                    // Sanity check
                    if (potentialBuildingPair.second != null &&
                        call.isInsideOf(listOf(potentialBuildingPair.second!!))
                    )
                        return false
                }
                return true
            }
        }
        return false
    }

    private fun resolveConstructor(className: String): KtPrimaryConstructor? {
        return (KotlinClassShortNameIndex
            .getInstance()
            .get(className, project, GlobalSearchScope.allScope(project))
            .firstOrNull() as KtClass?)?.primaryConstructor
    }

    private fun resolveConstructor(call: KtCallElement): KtPrimaryConstructor? {
        return call.firstChild?.text?.let { resolveConstructor(it) }
    }

    private fun findBuildAndBuilderByClassName(
        dataClassName: String?
    ): Pair<KtNamedFunction, KtClass?>? {

        if (dataClassName == null)
            return null

        val potentialBuilder: KtClass? =
            KotlinClassShortNameIndex
                .getInstance()
                .get("${dataClassName}Builder", project, GlobalSearchScope.allScope(project))
                .firstOrNull() as KtClass?

        val potentialBuildFunction: KtNamedFunction =
            KotlinFunctionShortNameIndex
                .getInstance()
                .get("build${dataClassName}", project, GlobalSearchScope.allScope(project))
                .firstOrNull() ?: return null

        return Pair(potentialBuildFunction, potentialBuilder)

    }

}