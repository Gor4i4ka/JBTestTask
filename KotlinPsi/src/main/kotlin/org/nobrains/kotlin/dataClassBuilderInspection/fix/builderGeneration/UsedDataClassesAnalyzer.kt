package org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.kotlin.idea.debugger.sequence.psi.callName
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveClassOrNull
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveFunctionOrNull

object UsedDataClassesAnalyzer {

    fun analyze(call: KtCallExpression): List<DataClassInfo> {
        val analysisResult = HashSet<DataClassInfo>()
        analyzeElementWithChildren(call, call.project, analysisResult)
        return analysisResult.toImmutableList()
    }

    private fun analyzeElementWithChildren(
        element: KtElement,
        project: Project,
        analysisResult: MutableSet<DataClassInfo>
    ) {

        // It is useful to analyze only KtNameReferenceExpression
        if (element is KtCallExpression)
            analyzeElement(element, project, analysisResult)
        for (child in element.children)
            analyzeElementWithChildren(child as KtElement, project, analysisResult)
    }

    private fun analyzeElement(element: KtCallExpression, project: Project, analysisResult: MutableSet<DataClassInfo>) {

        val callName = element.callName()
        val correspondingDataClass = resolveClassOrNull(callName, project)

        if (correspondingDataClass != null && correspondingDataClass.isData()) {
            val hasPotentialBuilder = resolveClassOrNull("${callName}Builder", project) != null
            val hasPotentialBuild = resolveFunctionOrNull("build${callName}", project) != null

            val dataClassInfo = DataClassInfo(
                clazz = correspondingDataClass,
                hasBuildFunction = hasPotentialBuild,
                hasBuilderClass = hasPotentialBuilder
            )

            analysisResult.add(dataClassInfo)
        }
    }
}