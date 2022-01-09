package testDsl.specialTests.largeClassTest.classes.personPack.goodPerson

import testDsl.specialTests.largeClassTest.classes.Movie
import kotlin.properties.Delegates

data class Person(
    val goodThingsDone: Int,
    val name: String,
    var surname: String?,
    val age: Int,
    var children: Int?,
    val favouriteMovies: List<Movie>,
    val favouriteQuotes: List<String>
)

fun buildPerson(init: PersonBuilder.() -> Unit): Person =
    PersonBuilder().apply(init).build()

class PersonBuilder {
    var goodThingsDone by Delegates.notNull<Int>()
    lateinit var name: String
    var surname: String? = null
    var age by Delegates.notNull<Int>()
    var children: Int? = null
    var favouriteMovies = mutableListOf<Movie>()
    fun favouriteMoviesElement(element: Movie) {
        favouriteMovies.add(element)
    }

    var favouriteQuotes = mutableListOf<String>()


    fun build(): Person =
        Person(goodThingsDone, name, surname, age, children, favouriteMovies, favouriteQuotes)
}