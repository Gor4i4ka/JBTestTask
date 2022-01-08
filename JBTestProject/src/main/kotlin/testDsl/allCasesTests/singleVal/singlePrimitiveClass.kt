package testDsl.allCasesTests.singleVal

import kotlin.properties.Delegates

data class SinglePrimitiveClass(val value: Int)

fun usage1() {
    val usage = SinglePrimitiveClass(0)
}