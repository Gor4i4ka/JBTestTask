package testDsl.allCasesTests.onlyPrimitives.singleVal

data class SinglePrimitiveClass(val value: Int)

fun usage1() {
    val usage = SinglePrimitiveClass(0)
}