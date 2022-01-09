package testDsl.allCasesTests.recursive.onlyDataClasses

data class Data1(val value: Int) {

    val property1: String = "valueString"
    var property2: StringBuilder? = null

    init {
        doSmth()
    }

    fun doSmth() {
        property2 = StringBuilder(property1)
    }
}

data class Data2(val complex1: String, val data1: Data1)

private val usage = Data2("ValueString", Data1(1))
