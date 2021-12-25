package javaStyleChecks

import kotlin.properties.Delegates

data class DataA(val valueA: Float, var valueB: Int?, val valueC: String, var valueD: Boolean?) {

}

data class DataB(val valueB: Int) {

}

class DataBBuilder {
    var valueB: Int? = null
    fun build(): DataB? {
        return if (valueB == null) null else DataB(valueB!!)
    }
}

data class DataC(val valueA: Int, var valueB: Float?, val collectionA: ArrayList<Int>)

fun buildDataC(init: DataCBuilder.() -> Unit): DataC =
    DataCBuilder().apply(init).build()

class DataCBuilder {
    var valueA by Delegates.notNull<Int>()
    var valueB: Float? = null
    lateinit var collectionA: ArrayList<Int>

    fun build(): DataC =
        DataC(valueA, valueB, collectionA)
}

fun main(args: Array<String>) {
    println("BEGIN")
    val b = listOf<DataB>(DataBBuilder().apply{valueB = 1; valueB = 1}.build()!!, DataB(2))
    println("END")
}
