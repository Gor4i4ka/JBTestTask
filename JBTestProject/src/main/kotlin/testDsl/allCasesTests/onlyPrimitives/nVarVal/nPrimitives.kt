package testDsl.allCasesTests.onlyPrimitives.nVarVal

data class NPrimitivesClass(
    val value1: Int, val value2: Float?, val value3: Boolean? = null,
    var variable1: Int, var variable2: Float? = null, var variable3: Boolean?
)

fun usage1() {
    val usage = NPrimitivesClass(1, null, false, 2, 3f, null)
}