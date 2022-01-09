package testDsl.specialTests.largeClassTest.classes.personPack.badPerson

import testDsl.specialTests.largeClassTest.classes.Movie

data class Person(
    val name: String,
    var surname: String?,
    val age: Int,
    var children: Int?,
    var bankAccountMoney: Float?,
    val favouriteMovies: List<Movie>,
    val favouriteQuotes: List<String>
)
