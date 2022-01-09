package testDsl.allCasesTests.recursive.onlyDataClasses

data class DataPrimitive(val valueInt: Int?, var valueFloat: Float)

data class DataComplex(
    val valueComplex1: StringBuilder?,
    val dataPrimitive1: DataPrimitive,
    val valueComplex2: StringBuilder,
    val dataPrimitive2: DataPrimitive
)

data class DataMix(
    val valueComplex1: String,
    val dataPrimitive1: DataPrimitive,
    val valueComplex2: StringBuilder? = StringBuilder("stringDefault"),
    val dataComplex1: DataComplex,
    val dataPrimitive2: DataPrimitive? = null,
    val primitive: Int?,
    val dataComplex2: DataComplex
)

private val usage = DataMix(
    "stringValue",
    DataPrimitive(null, 2f),
    dataComplex1 = DataComplex(
        null,
        valueComplex2 = StringBuilder("valueComplex2"),
        dataPrimitive1 = DataPrimitive(1, 3f),
        dataPrimitive2 = DataPrimitive(3, 4f)
    ),
    dataPrimitive2 = DataPrimitive(
        valueInt = null,
        valueFloat = 6f
    ),
    dataComplex2 = DataComplex(
        StringBuilder("valueComplex2"),
        DataPrimitive(4, 7f),
        StringBuilder("valueComplex2"),
        DataPrimitive(10, 2.5f),
    ),
    primitive = 5
)

