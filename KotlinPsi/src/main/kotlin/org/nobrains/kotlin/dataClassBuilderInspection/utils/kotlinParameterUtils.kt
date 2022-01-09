package org.nobrains.kotlin.dataClassBuilderInspection.utils

import org.jetbrains.kotlin.psi.KtParameter

fun findParameterByName(name: String, parameterList: List<KtParameter>) : KtParameter? {
    for (parameter in parameterList) {
        if (parameter.nameAsSafeName.toString() == name)
            return parameter
    }
    return null
}