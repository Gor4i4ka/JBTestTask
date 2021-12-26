package localTests

import kotlin.properties.Delegates

data class DataA(val valueA: Int)

data class DataB(val valueB: Int, val dataA: DataA)


class DataBBuilder {
    var valueB by Delegates.notNull<Int>()
    private lateinit var _dataA: DataA
    var dataA: DataA
        get() = _dataA
        set(value) {
            _dataA = value
        }

    fun build(): DataB = DataB(valueB, dataA)
}


fun main(args: Array<String>) {
    println("BEGIN")
    println("END")
}
