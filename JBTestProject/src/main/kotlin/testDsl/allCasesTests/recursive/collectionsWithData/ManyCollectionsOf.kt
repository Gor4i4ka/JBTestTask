package testDsl.allCasesTests.recursive.collectionsWithData

// An ugly duplicate of LargeTest

data class Crowd(
    val crowdName: String,
    val people: List<Person>,
    val forMovies: Set<Movie>
)


data class Person(
    val name: String,
    var surname: StringBuilder?,
    val age: Int,
    var children: Int?,
    var bankAccountMoney: Float?,
    val favouriteMovies: List<Movie>,
    val favouriteQuotes: List<String>
)


data class Movie(
    val title: String,
    val budget: Float
)


// Use cite BEFORE

val crowdUsage = Crowd(
    "BOIZ",
    listOf(
        Person(
            "Den",
            surname = StringBuilder("Serditiy"),
            23,
            null,
            -100f,
            listOf(
                Movie(
                    budget = 100f,
                    title = "Titanic"
                ),
                Movie(
                    "slonik",
                    100f
                )
            ),
            listOf(
                "boi",
                "next door"
            )
        ),
        Person(

            bankAccountMoney = 1000f,
            surname = StringBuilder("Kozlov"),
            age = 20,
            favouriteMovies = listOf(),
            name = "AiWeeWee",
            children = null,
            favouriteQuotes = listOf(
                "kek",
            )
        )
    ),
    setOf(
        Movie(
            "Titanic",
            100f
        ),
        Movie(
            "slonik",
            100f
        )
    )
)


// Person builders

fun main() {

    println(crowdUsage)
}