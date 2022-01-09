package testDsl.allCasesTests.nonRecursive.onlyPrimitives.singleVar

data class SinglePrimitiveClass(var value: Int)

fun usage1() {
    val usage = SinglePrimitiveClass(1)
}