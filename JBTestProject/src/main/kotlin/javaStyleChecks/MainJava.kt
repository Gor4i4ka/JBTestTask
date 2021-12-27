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

fun buildDataB(init: DataBBuilder.() -> Unit): DataB =
    DataBBuilder().apply(init).build()

class DataBBuilder {
    var valueB by Delegates.notNull<Int>()

    fun build(): DataB =
        DataB(valueB)
}


data class DataC(
    val valueA: Int, var valueB: Float?, val complexA: String,
    val complexB: String?, val dataA: DataA, val collectionA: List<Int>, val collectionB: List<DataA>
)

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


data class DataD(val valueA: Int, val dataB: DataB)

fun buildDataD(init: DataDBuilder.() -> Unit): DataD =
    DataDBuilder().apply(init).build()

class DataDBuilder {
    var valueA by Delegates.notNull<Int>()
    lateinit var dataB: DataB

    fun build(): DataD =
        DataD(valueA, dataB)
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
            valueD = null
        }

        collectionBElement {
            valueA = 1f
            valueB = null
            valueC = "NOBRAINS"
            valueD = null
        }
    }
    val data = DataB(1)
    //DataD(1, DataB(2))
    val dataD = DataD(1, DataB(2))

//    val dataC = DataC(1, null, "Boi", "Voi", DataA(1f, null, "Brains",
//    true), listOf(1, 2), listOf(DataA(1f, null, "Brains",
//        true))
//    )

    val dataC = DataC(1, null, "Boi", "Voi", DataA(1f, null, "Brains",
        true), listOf(1, 2), listOf(DataA(1f, null, "Brains",
        true))
    )


//    val dataC = buildDataC {
//        valueA = 1
//        valueB = null
//        complexA = "Boi"
//        complexB = "Voi"
//        dataA = buildDataA {
//            valueA = 1f
//            valueB = null
//            valueC = "Brains"
//            valueD = true
//        }
//        collectionA = buildCollection {}
//        collectionB = buildCollection {}
//    }

    println(BOI)
    println("END")
}
