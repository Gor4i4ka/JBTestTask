package testDsl.allCasesTests.nonRecursive.onlyPrimitives.singleVar

data class SinglePrimitiveDefaultClass(var value: Int = 0)

fun usage3() {
    val usage = SinglePrimitiveDefaultClass(0)
}