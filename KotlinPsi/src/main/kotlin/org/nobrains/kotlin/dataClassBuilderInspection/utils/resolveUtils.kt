package org.nobrains.kotlin.dataClassBuilderInspection.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.*

fun resolveClassOrNull(className: String, project: Project): KtClass? {
    return KotlinClassShortNameIndex
        .getInstance()
        .get(className, project, GlobalSearchScope.allScope(project))
        .firstOrNull() as KtClass?
}

fun resolveFunctionOrNull(functionName: String, project: Project): KtNamedFunction? {
    return KotlinFunctionShortNameIndex
        .getInstance()
        .get(functionName, project, GlobalSearchScope.allScope(project))
        .firstOrNull()
}

fun resolveConstructorOrNull(call: KtCallElement): KtPrimaryConstructor? {
    //return call.resolveToCall()?.resultingDescriptor?.findPsi() as? KtPrimaryConstructor
    return (resolveClassOrNull(call.firstChild.text, call.project) as KtClass).primaryConstructor
}

fun findBuilderAndBuildForClass(dataClassName: String?, project: Project): Pair<KtNamedFunction, KtClass?>? {

    if (dataClassName == null)
        return null

    val potentialBuilder = resolveClassOrNull("${dataClassName}Builder", project)
    val potentialBuildFunction: KtNamedFunction? = resolveFunctionOrNull("build${dataClassName}", project)

    return if (potentialBuildFunction != null)
        Pair(potentialBuildFunction, potentialBuilder)
    else
        null

}

