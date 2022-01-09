package testDsl.allCasesTests.onlyPrimitives.namedVarVals

data class SingleValue(val value: Int)

fun usage1() {
    SingleValue(value = 1)
}