package testDsl.allCasesTests.nonRecursive.complexWithPrimitives.singleComplex

data class SingleComplexDefaultClass(val value: String = "valueStringDefault")

fun buildSingleComplexDefaultClass(init: SingleComplexDefaultClassBuilder.() -> Unit): SingleComplexDefaultClass =
    SingleComplexDefaultClassBuilder().apply(init).build()

class SingleComplexDefaultClassBuilder {
    lateinit var value: String

    fun build(): SingleComplexDefaultClass =
        SingleComplexDefaultClass(value)
}

fun usage2() {
    val usage = buildSingleComplexDefaultClass {
    }
}