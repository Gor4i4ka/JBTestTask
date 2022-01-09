package testDsl.specialTests

data class CrowdLocal(
    val crowdName: String,
    val people: List<PersonLocal>,
    val forMovies: Set<MovieLocal>
)


data class PersonLocal(
    val name: String,
    var surname: StringBuilder?,
    val age: Int,
    var children: Int?,
    var bankAccountMoney: Float?,
    val favouriteMovies: List<MovieLocal>,
    val favouriteQuotes: List<String>
)


data class MovieLocal(
    val title: String,
    val budget: Float
)


// Use cite BEFORE

val crowdUsage = CrowdLocal(
    "BOIZ",
    listOf(
        PersonLocal(
            "Den",
            surname = StringBuilder("Serditiy"),
            23,
            null,
            -100f,
            listOf(
                MovieLocal(
                    budget = 100f,
                    title = "Titanic"
                ),
                MovieLocal(
                    "slonik",
                    100f
                )
            ),
            listOf(
                "boi",
                "next door"
            )
        ),
        PersonLocal(

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
        MovieLocal(
            "Titanic",
            100f
        ),
        MovieLocal(
            "slonik",
            100f
        )
    )
)


// Person builders

fun main() {

    println(crowdUsage)
}