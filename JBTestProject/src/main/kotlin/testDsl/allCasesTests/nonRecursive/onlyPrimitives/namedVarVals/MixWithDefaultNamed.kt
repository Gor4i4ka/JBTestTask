package testDsl.allCasesTests.nonRecursive.onlyPrimitives.namedVarVals

data class MixPositionedDefault(val value1: Int = 2, val value2: Float?, val value3: Int? = null,
                         val value4: Long, val value5: Int = 3, val value6: Float)

fun usage4() {
    val usage = MixPositioned(0, value6 = 2f, value5= 3, value4 = 7, value2 = null, value3 = 5)
}