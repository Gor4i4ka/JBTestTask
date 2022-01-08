package org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration

import com.intellij.openapi.project.Project
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.kotlin.idea.debugger.sequence.psi.callName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.nobrains.kotlin.dataClassBuilderInspection.utils.findReferenceBuilderAndBuildForClass
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveIndexClassOrNull
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveIndexFunctionOrNull
import org.nobrains.kotlin.dataClassBuilderInspection.utils.resolveReferenceClassOrNull

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
        //val correspondingDataClass = resolveClassOrNull(callName, project)
        val correspondingDataClass = resolveReferenceClassOrNull(element)

        if (correspondingDataClass != null && correspondingDataClass.isData()) {
//            val hasPotentialBuilder = resolveIndexClassOrNull("${callName}Builder", project) != null
//            val hasPotentialBuild = resolveIndexFunctionOrNull("build${callName}", project) != null

            val builderMechanism = findReferenceBuilderAndBuildForClass(correspondingDataClass)
            val hasPotentialBuilder = builderMechanism?.first != null
            val hasPotentialBuild = builderMechanism?.second != null

            val dataClassInfo = DataClassInfo(
                clazz = correspondingDataClass,
                hasBuildFunction = hasPotentialBuild,
                hasBuilderClass = hasPotentialBuilder
            )

            analysisResult.add(dataClassInfo)
        }
    }
}