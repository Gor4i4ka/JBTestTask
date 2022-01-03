package org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveClassOrNull

object UsedDataClassesAnalyzer {

    fun analyze(call: KtCallElement): List<DataClassInfo> {
        val analysisResult = HashSet<DataClassInfo>()
        analyzeElementWithChildren(call, call.project, analysisResult)
        return analysisResult.toImmutableList()
    }

    private fun analyzeElementWithChildren(element: KtElement, project: Project, analysisResult: MutableSet<DataClassInfo>) {

        // It is useful to analyze only KtNameReferenceExpression
        if (element is KtCallElement)
            analyzeElement(element, project, analysisResult)
        for (child in element.children)
            analyzeElementWithChildren(child as KtElement, project, analysisResult)
    }

    private fun analyzeElement(element: KtCallElement, project: Project, analysisResult: MutableSet<DataClassInfo>) {

        val callName = element.firstChild?.text
        val correspondingDataClass: KtClass? = callName?.let { resolveClassOrNull(it, project) }

        if (correspondingDataClass != null && correspondingDataClass.isData()) {
            val hasPotentialBuilder =
                !KotlinClassShortNameIndex
                    .getInstance()
                    .get("${callName}Builder", project, GlobalSearchScope.allScope(project))
                    .isEmpty()
            val hasPotentialBuild =
                !KotlinFunctionShortNameIndex
                    .getInstance()
                    .get("build${callName}", project, GlobalSearchScope.allScope(project))
                    .isEmpty()

            val dataClassInfo = DataClassInfo(
                clazz = correspondingDataClass,
                hasBuildFunction = hasPotentialBuild,
                hasBuilderClass = hasPotentialBuilder
            )

            analysisResult.add(dataClassInfo)
        }
    }
}