package org.nobrains.kotlin.dataClassBuilderInspection.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex
import org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass

fun resolveIndexClassOrNull(className: String, project: Project): KtClass? {
    return KotlinClassShortNameIndex
        .getInstance()
        .get(className, project, GlobalSearchScope.allScope(project))
        .firstOrNull() as? KtClass
}

fun resolveIndexClassWithBuilderOrNull(className: String, project: Project): KtClass? {
    val potentialClasses = KotlinClassShortNameIndex
        .getInstance()
        .get(className, project, GlobalSearchScope.allScope(project))

    val potentialBuilder = findIndexBuilderAndBuildForClass(className, project)?.second

    for (element in potentialClasses)
        if (element.containingFile == potentialBuilder?.containingFile)
            return element as? KtClass

    return null
}

fun resolveIndexFunctionOrNull(functionName: String, project: Project): KtNamedFunction? {
    return KotlinFunctionShortNameIndex
        .getInstance()
        .get(functionName, project, GlobalSearchScope.allScope(project))
        .firstOrNull()
}

fun resolveIndexConstructorOrNull(call: KtCallElement): KtPrimaryConstructor? {
    return (resolveIndexClassOrNull(call.firstChild.text, call.project))?.primaryConstructor
}

fun resolveIndexConstructorWithBuilderOrNull(call: KtCallExpression): KtPrimaryConstructor? {
    return (resolveIndexClassWithBuilderOrNull(call.firstChild.text, call.project))?.primaryConstructor
}

fun findIndexBuilderAndBuildForClass(dataClassName: String?, project: Project): Pair<KtNamedFunction, KtClass>? {

    if (dataClassName == null)
        return null

    val potentialBuilder = resolveIndexClassOrNull("${dataClassName}Builder", project)
    val potentialBuildFunction: KtNamedFunction? = resolveIndexFunctionOrNull("build${dataClassName}", project)

    return if (potentialBuildFunction != null && potentialBuilder != null)
        Pair(potentialBuildFunction, potentialBuilder)
    else
        null

}

//

fun resolveReferenceClassOrNull(call: KtCallExpression): KtClass? {
    return resolveReferenceConstructorOrNull(call)?.containingClass()
}

fun resolveReferenceConstructorOrNull(call: KtCallExpression): KtPrimaryConstructor? {
    val primaryConstructorDeclarationDescriptor = call.resolveToCall()?.resultingDescriptor
    val primaryConstructorDeclaration = primaryConstructorDeclarationDescriptor?.findPsi() as? KtPrimaryConstructor

    return primaryConstructorDeclaration
}

fun findLocalBuilderAndBuildForClass(clazz: KtClass): Pair<KtNamedFunction, KtClass>? {

    val className = clazz.nameAsSafeName
    val buildFunctionName = "build${className}"
    val builderClassName = "${className}Builder"

    var buildFunction: KtNamedFunction? = null
    var builderClass: KtClass? = null

    val containingKtElement = clazz.parent

    for (element in containingKtElement.children) {
        // Checking for "build" Function
        if (element is KtNamedFunction && element.nameAsSafeName.toString() == buildFunctionName) {
            buildFunction = element
            continue
        }

        if (element is KtClass && element.nameAsSafeName.toString() == builderClassName) {
            builderClass = element
            continue
        }
    }

    return if (buildFunction != null && builderClass != null)
        Pair(buildFunction, builderClass)
    else
        null
}

