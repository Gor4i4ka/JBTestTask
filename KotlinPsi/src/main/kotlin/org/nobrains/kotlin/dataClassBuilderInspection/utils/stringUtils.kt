package org.nobrains.kotlin.dataClassBuilderInspection.utils

fun String.toMutableInstantiationByName(): String {
    return "mutable${this[0].uppercase()}${this.drop(1)}\n"
}