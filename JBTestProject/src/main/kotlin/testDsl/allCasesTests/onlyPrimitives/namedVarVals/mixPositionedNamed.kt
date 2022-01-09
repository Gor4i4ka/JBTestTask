package testDsl.allCasesTests.onlyPrimitives.namedVarVals

data class MixPositioned(val value1: Int, val value2: Float?, val value3: Int?,
                         val value4: Long, val value5: Int, val value6: Float)

fun usage3() {
    val usage = MixPositioned(1, value6 = 2f, value5= 3, value4 = 7, value2 = null, value3 = 5)
}