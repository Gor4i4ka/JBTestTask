package testDsl.allCasesTests.singleVar

data class SinglePrimitiveDefaultClass(val value: Int = 0)

fun usage3() {
    val usage = SinglePrimitiveDefaultClass(0)
}