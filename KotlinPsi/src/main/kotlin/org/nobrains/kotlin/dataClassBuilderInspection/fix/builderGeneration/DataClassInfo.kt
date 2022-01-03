package org.nobrains.kotlin.dataClassBuilderInspection.fix.builderGeneration

import org.jetbrains.kotlin.psi.KtClass

data class DataClassInfo(val clazz: KtClass, val hasBuildFunction: Boolean, val hasBuilderClass: Boolean)