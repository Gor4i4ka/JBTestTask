package testDsl.allCasesTests.nonRecursive.complexWithPrimitives.namedComplex

data class MixComplexPrimitive(
    val complex1: String,
    val primitive1: Int?,
    val complex2: StringBuilder,
    val complex3: StringBuilder? = null,
    val primitive2: Float,
    var complex4: String,
    var complex5: StringBuilder? = null,
    var primitive3: Boolean? = null,
    var complex6: String?
)

fun usage2() {
    val usage = MixComplexPrimitive(
        "Value1",
        null,
        complex6 = null,
        primitive2 = 2f,
        complex3 = null,
        complex4 = "Variable1",
        complex2 = StringBuilder("Value2"),
        complex5 = StringBuilder("Value3")
    )
}