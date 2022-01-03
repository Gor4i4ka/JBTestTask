package org.nobrains.kotlin.dataClassBuilderInspection

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.findFunctionByName
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf
import org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration.UsedDataClassesAnalyzer
import org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration.UsedDataClassesBuilderCreator
import org.nobrains.kotlin.dataClassBuilderInspection.utils.findBuilderAndBuildForClass
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveConstructorOrNull

class BuilderUsagesInspection :
    AbstractApplicabilityBasedInspection<KtNameReferenceExpression>(KtNameReferenceExpression::class.java) {

    private val handledCollectionsOf = setOf("listOf", "setOf", "stackOf")

    override val defaultFixText: String
        get() = "Wrap this data class usage in a DSL-style builder"

    override fun inspectionText(element: KtNameReferenceExpression): String {
        return "Generates DSL-builders for the data class and wraps this usage"
    }

    override fun applyTo(element: KtNameReferenceExpression, project: Project, editor: Editor?) {

        // Firstly Generate all builders
        val analysisResult = UsedDataClassesAnalyzer.analyze(element.parent as KtCallElement)
        UsedDataClassesBuilderCreator.createDslBuilders(analysisResult)

        element.parent.replace(applyTo(element.parent as KtCallElement))
    }

    override fun isApplicable(element: KtNameReferenceExpression): Boolean {

        val parent = element.parent
        if (parent is KtCallElement && element.text !in handledCollectionsOf)
            return isApplicable(parent)
        return false
    }

    private fun isApplicable(call: KtCallElement): Boolean {

        val potentialConstructor = resolveConstructorOrNull(call)

        if (potentialConstructor is KtPrimaryConstructor) {
            val clazz = potentialConstructor.parent as KtClass
            if (clazz.isData()) {
                val potentialBuildingPair = findBuilderAndBuildForClass(
                    clazz.nameAsSafeName.toString(),
                    clazz.project
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
}