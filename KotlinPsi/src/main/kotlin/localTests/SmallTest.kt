package localTests

// Data Classes
data class Person(
    val name: String,
    val favouriteMovies: List<Movie>,
)
data class Movie(
    val title: String,
)

// Use cite BEFORE
val personBefore = Person(
    "John",
    favouriteMovies = listOf(
        Movie("The Lord of the Rings"),
        Movie("Pulp Fiction"),
    ))

// Person builders

fun buildPerson(init: PersonBuilder.() -> Unit): Person =
    PersonBuilder().apply(init).build()

class PersonBuilder() {

    lateinit var name: String
    private val favouriteMovies = mutableListOf<Movie>()
    fun favouriteMovie(init: MovieBuilder.() -> Unit) {
        //favouriteMovies += MovieBuilder().apply(init).build()
        favouriteMovies += buildMovie(init)
    }
    fun build(): Person = Person(name, favouriteMovies)
}

// Movie builders
fun buildMovie(init: MovieBuilder.() -> Unit): Movie =
    MovieBuilder().apply(init).build()

class MovieBuilder {
    lateinit var title: String
    fun build(): Movie = Movie(title)
}

val personAfter: Person = buildPerson {
    name = "John"
    favouriteMovie {
        title = "The Lord of the Rings"
    }
    favouriteMovie {
        title = "Pulp Fiction"
    }
}

fun main() {

    println(personAfter)
}