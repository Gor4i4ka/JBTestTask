package testDsl.allCasesTests.nonRecursive.complexWithPrimitives.nArguments

data class NComplexClass(
    val value1: String, val value2: StringBuilder, val value3: StringBuilder? = null,
    var variable1: String, var variable2: StringBuilder? = null, var variable3: String?
)

fun usage1() {
    val usage = NComplexClass(
        "first", StringBuilder("Second"),
        null, "Second", StringBuilder("varSecond"), null
    )
}