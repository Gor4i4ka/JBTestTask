package stuff

import java.lang.StringBuilder

data class DataA(val valueA: Float, var valueB: Int?, val valueC: String, var valueD: Boolean?) {

}

class ClassA(val valueA: Int, var variableB: Float?) {

}

data class DataB(val valueB: Int) {

}

class DataBBuilder {
    var valueB: Int? = null
    fun build(): DataB? {
        return if (valueB == null) null else DataB(valueB!!)
    }
}

class ClassB(val valueB: Int) {

}

fun func(variable: Int): Int {
    return 1
}

fun main(args: Array<String>) {
    println("BEGIN")
    val dba = DataABuilder()
    val da = dba.build()
    val b = listOf<DataB>(DataBBuilder().apply{valueB = 1; valueB = 1}.build()!!, DataB(2))
    println("END")
}
