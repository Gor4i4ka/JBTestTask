package org.nobrains.kotlin.dataClassBuilderInspection.utils

import org.jetbrains.kotlin.idea.quickfix.ConvertCollectionFix.Companion.getCollectionType
import org.jetbrains.kotlin.types.KotlinType

fun KotlinType.extractCollectionArgumentNameOrNull(): String? {
    return try {
        this.arguments[0].toString()
    } catch (exception: IndexOutOfBoundsException) {
        null
    }
}

fun KotlinType.extractCollectionNameOrNull(): String? {
    return this.getCollectionType()?.name
}