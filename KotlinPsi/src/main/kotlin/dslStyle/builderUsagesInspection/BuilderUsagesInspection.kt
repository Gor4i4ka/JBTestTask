package dslStyle.builderUsagesInspection

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf

class BuilderUsagesInspection :
    AbstractApplicabilityBasedInspection<KtNameReferenceExpression>(KtNameReferenceExpression::class.java) {

    private lateinit var kotlinFactory: KtPsiFactory
    private lateinit var project: Project

    private var debugFlag: Boolean? = null
    private val handledCollectionsOf = setOf<String>("listOf")
    private val handledCollections = setOf<String>("List")

    override val defaultFixText: String
        get() = "Wrap the constructor call with the DSL-style builder"

    override fun applyTo(element: KtNameReferenceExpression, project: Project, editor: Editor?) {
        kotlinFactory = KtPsiFactory(project)
        this.project = element.project
        element.parent.replace(applyTo(element.parent as KtCallElement))
    }

    private fun applyTo(call: KtCallElement): KtCallElement {

        val wrapperStringBuilder = StringBuilder()

        val resolvedConstructor = resolveConstructor(call)!!
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
            .append("\n}")

        // Creating the expression and processing children

        val wrapperExpression = kotlinFactory.createExpression(wrapperStringBuilder.toString()) as KtCallElement
        visitChildren(wrapperExpression)

        return wrapperExpression
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

            // We are safe if building function is found
            if (potentialBuilderPair != null) {
                val genericPrimaryConstructor = resolveConstructor(genericType!!)
                val genericPrimaryConstructorParameters = genericPrimaryConstructor?.valueParameters!!

                val wrapperForField = StringBuilder()
                    .append("${parameter.nameAsSafeName}Element ")
                    .append("(\n")
                    .apply {
                        for (argumentIndex in argumentExpression?.valueArguments?.indices!!) {
                            append(
                                argumentExpression.valueArguments[argumentIndex].getArgumentExpression()?.text
                            )
                        }
                    }
                    .append("\n)\n")

                return wrapperForField.toString()
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

    override fun inspectionText(element: KtNameReferenceExpression): String {
        return "Wraps data class' primary constructor call with a DSL-style builder if one is present in the project."
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

                    return true
                }
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