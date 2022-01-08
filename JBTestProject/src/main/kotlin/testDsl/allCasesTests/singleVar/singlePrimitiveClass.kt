package testDsl.allCasesTests.singleVar

data class SinglePrimitiveClass(var value: Int, val v: Int)

fun usage1() {
    val usage = SinglePrimitiveClass(0, 1)
}