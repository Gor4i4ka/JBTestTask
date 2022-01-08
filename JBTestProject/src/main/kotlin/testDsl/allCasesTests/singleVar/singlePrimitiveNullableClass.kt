package testDsl.allCasesTests.singleVar

data class SinglePrimitiveNullableClass(val value: Int?)

fun usage2() {
    val usage = SinglePrimitiveNullableClass(null)
}