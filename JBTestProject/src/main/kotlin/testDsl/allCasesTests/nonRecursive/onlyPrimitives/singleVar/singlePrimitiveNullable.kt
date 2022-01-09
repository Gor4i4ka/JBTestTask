package testDsl.allCasesTests.nonRecursive.onlyPrimitives.singleVar

data class SinglePrimitiveNullableClass(var value: Int?)

fun usage2() {
    val usage = SinglePrimitiveNullableClass(null)
}