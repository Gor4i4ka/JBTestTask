package testDsl.allCasesTests.nonRecursive.onlyPrimitives.singleVal

data class SinglePrimitiveClass(val value: Int)

fun usage1() {
    val usage = SinglePrimitiveClass(0)
}