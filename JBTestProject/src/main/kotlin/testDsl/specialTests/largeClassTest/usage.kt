package testDsl.specialTests.largeClassTest
import testDsl.specialTests.largeClassTest.classes.Crowd
import testDsl.specialTests.largeClassTest.classes.Movie
import testDsl.specialTests.largeClassTest.classes.personPack.badPerson.Person

val crowdUsage = Crowd(
    "BOIZ",
    listOf(
        Person(
            "Den",
            "Serditiy",
            23,
            null,
            -100f,
            listOf(
                Movie(
                    "Titanic",
                    100f
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
            "AiWeeWee",
            "drug",
            20,
            2,
            1000f,
            listOf(
                Movie(
                    "Anime",
                    100f
                ),
            ),
            listOf(
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

fun main() {
    println(crowdUsage)
}