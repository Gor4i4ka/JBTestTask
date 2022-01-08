package org.nobrains.kotlin.dataClassBuilderInspection

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.caches.resolve.findModuleDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.debugger.sequence.psi.callName
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInsideOf
import org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration.UsedDataClassesAnalyzer
import org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration.UsedDataClassesBuilderCreator
import org.nobrains.kotlin.dataClassBuilderInspection.fix.callWrapping.DataClassCallWrapper
import org.nobrains.kotlin.dataClassBuilderInspection.utils.findIndexBuilderAndBuildForClass
import org.nobrains.kotlin.dataClassBuilderInspection.utils.findLocalBuilderAndBuildForClass
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveReferenceClassOrNull
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveReferenceConstructorOrNull

class BuilderUsagesInspection :
    AbstractApplicabilityBasedInspection<KtNameReferenceExpression>(KtNameReferenceExpression::class.java) {

    private val handledCollectionsOf = setOf("listOf", "setOf", "stackOf")

    override val defaultFixText: String
        get() = "Wrap this data class usage in a DSL-style builder"

    override fun inspectionText(element: KtNameReferenceExpression): String {
        return "Generates DSL-builders for the data class and wraps this usage"
    }

    override fun applyTo(element: KtNameReferenceExpression, project: Project, editor: Editor?) {

        val call = element.parent as? KtCallExpression?: return

        // Firstly Generate all builders
        val analysisResult = UsedDataClassesAnalyzer.analyze(call)
        UsedDataClassesBuilderCreator.createDslBuilders(analysisResult)

        // Secondly replace
        call.replace(DataClassCallWrapper.wrapConstructorCall(call))
    }

    override fun isApplicable(element: KtNameReferenceExpression): Boolean {

        val call = element.parent
        if (call is KtCallExpression && call.callName() !in handledCollectionsOf) {
            val resolvedClass = resolveReferenceClassOrNull(call)
            if (resolvedClass?.isData() == true) {
                val potentialBuildingPair = findLocalBuilderAndBuildForClass(resolvedClass)
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

}
