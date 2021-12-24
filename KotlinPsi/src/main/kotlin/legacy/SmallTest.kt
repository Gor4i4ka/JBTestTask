package legacy

fun funcOne() {
    println(funcTwo(1, 2f))
}

fun funcTwo(arg1: Int, arg2: Float?): Int {
    return arg1 + arg2?.toInt()!!
}