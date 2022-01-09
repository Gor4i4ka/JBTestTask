package testDsl.allCasesTests.recursive.collectionsWithData

data class DataInt(val valueInt: Int?)

data class CollectionClass(val dataIntList: List<DataInt>)

private val usage = CollectionClass(
    listOf(
        DataInt(1),
        DataInt(null),
        DataInt(3),
        DataInt(10),
        DataInt(null)
    )
)