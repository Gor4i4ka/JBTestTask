package testDsl.allCasesTests.nonRecursive.onlyPrimitives.namedVarVals

data class CorrectlyPositioned(val value1: Int, val value2: Float?, val value3: Int?, val value4: Long)

fun usage2() {
    val usage = CorrectlyPositioned(1, value2 = 2F, 3, value4 = 4L)
}