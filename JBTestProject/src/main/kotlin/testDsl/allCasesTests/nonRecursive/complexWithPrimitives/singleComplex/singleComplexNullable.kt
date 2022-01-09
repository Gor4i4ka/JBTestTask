package testDsl.allCasesTests.nonRecursive.complexWithPrimitives.singleComplex

data class SingleComplexNullableClass(var value: String?)

fun usage3() {
    val usage = SingleComplexNullableClass(null)
}