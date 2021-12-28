package testDsl

data class Crowd(
    val crowdName: String,
    val people: List<Person>,
    val forMovies: Set<Movie>
)

data class Person(
    val name: String,
    var surname: String?,
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
    "MY BOOOIZ",
    listOf(
        Person(
            "Denchik",
            "Durachok",
            23,
            null,
            -1_000_000F,
            listOf(
                Movie("Chelovek Pavuk", 1000f),
                Movie("Zeleniy slonik", 100000000f),
                Movie("TikTok vidosik", 0f)
            ),
            listOf(
                "AUF",
                "kek"
            )
        ),
        Person(
            "Andrew",
            "The Otchislenniy",
            25,
            8,
            1000f,
            listOf(
                Movie("vlastelin kolez", 2833f),
                Movie("GachiMuchi", 1f),
            ),
            listOf(
                "AUF",
                "Meee",
                "Poluchaetsa tak."
            )
        ),

        Person(
            "Boi Next Door",
            "Darkholm",
            40,
            null,
            100000f,
            listOf(),
            listOf(
                "Deep Dark Phantasies",
                "300 bucks"
            )
        )
    ),
    setOf(
        Movie("Titanic", 123f),
        Movie("Tonunik", 123f)
    )
)

// Person builders

fun main() {

    println(crowdUsage)
}