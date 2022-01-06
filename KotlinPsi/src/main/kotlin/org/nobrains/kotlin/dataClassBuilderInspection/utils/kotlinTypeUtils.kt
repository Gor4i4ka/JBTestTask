package org.nobrains.kotlin.dataClassBuilderInspection.utils

import org.jetbrains.kotlin.idea.quickfix.ConvertCollectionFix.Companion.getCollectionType
import org.jetbrains.kotlin.types.KotlinType

fun KotlinType.extractCollectionArgumentName(): String {
    return this.arguments[0].toString()
}

fun KotlinType.extractCollectionNameOrNull(): String? {
    return this.getCollectionType()?.name
}