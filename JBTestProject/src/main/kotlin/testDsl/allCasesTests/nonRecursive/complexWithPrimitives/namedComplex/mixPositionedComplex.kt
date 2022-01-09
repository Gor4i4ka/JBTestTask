package testDsl.allCasesTests.nonRecursive.complexWithPrimitives.namedComplex

data class MixPositionedComplex(
    val value1: String, val value2: StringBuilder, val value3: StringBuilder? = null,
    var variable1: String, var variable2: StringBuilder? = null, var variable3: String?
)

fun usage1() {
    val usage = MixPositionedComplex(
        "Value1",
        variable3 = null,
        variable2 = null,
        variable1 = "Variable1",
        value2 = StringBuilder("Value2"),
        value3 = StringBuilder("Value3")
    )
}