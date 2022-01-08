package testDsl.allCasesTests.singleVal

import kotlin.properties.Delegates

data class SinglePrimitiveClass(val value: Int)

fun buildSinglePrimitiveClass(init: SinglePrimitiveClassBuilder.() -> Unit): SinglePrimitiveClass =
    SinglePrimitiveClassBuilder().apply(init).build()

class SinglePrimitiveClassBuilder {
    var value by Delegates.notNull<Int>()

    fun build(): SinglePrimitiveClass =
        SinglePrimitiveClass(value)
}


fun usage1() {
    val usage = buildSinglePrimitiveClass {
        value = 0
    }
}