package dslStyle.builderUsagesInspection

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf

class BuilderUsagesInspection :
    AbstractApplicabilityBasedInspection<KtNameReferenceExpression>(KtNameReferenceExpression::class.java) {

    private lateinit var kotlinFactory: KtPsiFactory
    private var debugFlag: Boolean? = null
    private val handledCollectionsOf = setOf<String>("listOf")
    private val handledCollections = setOf<String>("List")

    override val defaultFixText: String
        get() = "Wrap the constructor call with the DSL-style builder"

    override fun applyTo(element: KtNameReferenceExpression, project: Project, editor: Editor?) {
        kotlinFactory = KtPsiFactory(project)
        element.parent.replace(applyTo(element.parent as KtCallElement, project))
    }

    private fun applyTo(call: KtCallElement, project: Project): KtCallElement {

        val wrapperStringBuilder = StringBuilder()

        val resolvedConstructor = resolveConstructor(call)!!
        val resolvedConstructorName = resolvedConstructor.nameAsSafeName.toString()
        val resolvedConstructorParameters = resolvedConstructor.valueParameters

        val resolvedBuildFunction = findBuildAndBuilderByClassName(resolvedConstructorName, project)?.first!!

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
        visitChildren(wrapperExpression, project)

        return wrapperExpression
    }

    private fun processField(parameter: KtParameter, argument: KtValueArgument): String {
        return "${parameter.nameAsSafeName} = ${argument.getArgumentExpression()?.text}\n"
    }

    private fun visitChildren(parent: KtElement, project: Project) {
        for (child in parent.children) {
            if (child is KtCallElement && isApplicable(child))
                child.replace(applyTo(child, project))
            visitChildren(child as KtElement, project)
        }
    }

    override fun inspectionText(element: KtNameReferenceExpression): String {
        return "Wraps data class' primary constructor call with a DSL-style builder if one is present in the project."
    }

    override fun isApplicable(element: KtNameReferenceExpression): Boolean {

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
                    clazz.nameAsSafeName.toString(),
                    clazz.project
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

    private fun resolveConstructor(call: KtCallElement): KtPrimaryConstructor? {
        return (KotlinClassShortNameIndex
            .getInstance()
            .get("${call.firstChild?.text}", call.project, GlobalSearchScope.allScope(call.project))
            .firstOrNull() as KtClass?)?.primaryConstructor
    }

    private fun findBuildAndBuilderByClassName(
        dataClassName: String?,
        project: Project
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