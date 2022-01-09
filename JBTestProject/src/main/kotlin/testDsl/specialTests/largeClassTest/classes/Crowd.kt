package testDsl.specialTests.largeClassTest.classes

import testDsl.specialTests.largeClassTest.classes.personPack.badPerson.Person

data class Crowd(
    val crowdName: String,
    val people: List<Person>,
    val forMovies: Set<Movie>
)
