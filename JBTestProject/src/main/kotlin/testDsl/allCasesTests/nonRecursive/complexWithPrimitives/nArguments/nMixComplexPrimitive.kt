package testDsl.allCasesTests.nonRecursive.complexWithPrimitives.nArguments

data class NComplexPrimitiveClass(
    val complex1: String, val primitive1: Int, val complex2: StringBuilder? = StringBuilder("stringDefault"),
    val primitive2: Float?, val complex3: StringBuilder? = null, val primitive: Int?
)

fun usage2() {
    val usage = NComplexPrimitiveClass(
        "first", 2,
        null, 3f, StringBuilder("varSecond"), null
    )
}