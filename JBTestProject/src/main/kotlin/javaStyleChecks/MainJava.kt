package javaStyleChecks

import kotlin.properties.Delegates

data class DataA(val valueA: Float, var valueB: Int?, val valueC: String, var valueD: Boolean?) {

}

fun buildDataA(init: DataABuilder.() -> Unit): DataA =
    DataABuilder().apply(init).build()

class DataABuilder {
    var valueA by Delegates.notNull<Float>()
    var valueB: Int? = null
    lateinit var valueC: String
    var valueD: Boolean? = null

    fun build(): DataA =
        DataA(valueA, valueB, valueC, valueD)
}

data class DataB(val valueB: Int) {

}

class DataBBuilder {
    var valueB: Int? = null
    fun build(): DataB? {
        return if (valueB == null) null else DataB(valueB!!)
    }
}

data class DataC(val valueA: Int, var valueB: Float?, val complexA: String,
                 val complexB: String?, val dataA: DataA, val collectionA: List<Int>, val collectionB: List<DataA>)

fun buildDataC(init: DataCBuilder.() -> Unit): DataC =
    DataCBuilder().apply(init).build()

class DataCBuilder {
    var valueA by Delegates.notNull<Int>()
    var valueB: Float? = null
    lateinit var complexA: String
    var complexB: String? = null
    lateinit var dataA: DataA
    private val collectionA = mutableListOf<Int>()
    private val collectionB = mutableListOf<DataA>()

    fun collectionBElement(init: DataABuilder.() -> Unit) {
        collectionB.add(buildDataA(init))
    }

    fun build(): DataC =
        DataC(valueA, valueB, complexA, complexB, dataA, collectionA, collectionB)
}


fun main(args: Array<String>) {
    println("BEGIN")
    val BOI = buildDataC {
        valueA = 2
        valueB = null
        complexA = "HOLA BOIZ"
        dataA = buildDataA {
            valueA = 2f
            valueB = 1
            valueC = "kek"
            valueD =  null
        }

        collectionBElement {
            valueA = 1f
            valueB = null
            valueC = "NOBRAINS"
            valueD =  null
        }
    }
    println(BOI)
    println("END")
}
