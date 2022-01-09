package testDsl.allCasesTests.onlyPrimitives.singleVal

data class SinglePrimitiveNullableClass(val value: Int?)

fun usage2() {
    val usage = SinglePrimitiveNullableClass(null)
}