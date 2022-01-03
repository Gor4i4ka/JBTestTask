package testDsl

import kotlin.properties.Delegates

data class Crowd(
    val crowdName: String,
    val people: List<Person>,
    val forMovies: Set<Movie>
)

fun buildCrowd(init: CrowdBuilder.() -> Unit): Crowd =
    CrowdBuilder().apply(init).build()

class CrowdBuilder {
    lateinit var crowdName: String
    var people = mutableListOf<Person>()
    fun peopleElement(element: Person) {
        people.add(element)
    }

    var forMovies = mutableSetOf<Movie>()
    fun forMoviesElement(element: Movie) {
        forMovies.add(element)
    }

    fun build(): Crowd =
        Crowd(crowdName, people, forMovies)
}

data class Person(
    val name: String,
    var surname: String?,
    val age: Int,
    var children: Int?,
    var bankAccountMoney: Float?,
    val favouriteMovies: List<Movie>,
    val favouriteQuotes: List<String>
)

fun buildPerson(init: PersonBuilder.() -> Unit): Person =
    PersonBuilder().apply(init).build()

class PersonBuilder {
    lateinit var name: String
    var surname: String? = null
    var age by Delegates.notNull<Int>()
    var children: Int? = null
    var bankAccountMoney: Float? = null
    var favouriteMovies = mutableListOf<Movie>()
    fun favouriteMoviesElement(element: Movie) {
        favouriteMovies.add(element)
    }

    var favouriteQuotes = mutableListOf<String>()


    fun build(): Person =
        Person(name, surname, age, children, bankAccountMoney, favouriteMovies, favouriteQuotes)
}

data class Movie(
    val title: String,
    val budget: Float
)

fun buildMovie(init: MovieBuilder.() -> Unit): Movie =
    MovieBuilder().apply(init).build()

class MovieBuilder {
    lateinit var title: String
    var budget by Delegates.notNull<Float>()

    fun build(): Movie =
        Movie(title, budget)
}


// Use cite BEFORE
val crowdUsage = buildCrowd {
    crowdName = "MY BOOOIZ"
    people = mutableListOf(
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
    )
    forMovies = mutableSetOf(
        Movie("Titanic", 123f),
        Movie("Tonunik", 123f)
    )
}

// Person builders

fun main() {

    println(crowdUsage)
}
