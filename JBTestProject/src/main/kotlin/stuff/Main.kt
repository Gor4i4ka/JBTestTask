package stuff
data class DataA(val valueA: Float, var valueB: Int?) {

}

class ClassA(val valueA: Int, var variableB: Float?) {

}

data class DataB(val valueB: Int) {

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
    val b = listOf<DataA>(DataA(1f, null), DataA(2f, 2))
    println("END")
}
