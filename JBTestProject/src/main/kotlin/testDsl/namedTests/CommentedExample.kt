package testDsl.namedTests
//
//data class Crowd(
//    val crowdName: String,
//    val people: List<Person>,
//    val forMovies: Set<Movie>
//)
//
//
//data class Person(
//    val name: String,
//    var surname: String?,
//    val age: Int,
//    var children: Int?,
//    var bankAccountMoney: Float?,
//    val favouriteMovies: List<Movie>,
//    val favouriteQuotes: List<String>
//)
//
//
//data class Movie(
//    val title: String,
//    val budget: Float
//)
//
//
//// Use cite BEFORE
//
//val crowdUsage = Crowd(
//    "BOIZ",
//    listOf(
//        Person(
//            "Den",
//            "Serditiy",
//            23,
//            null,
//            -100f,
//            listOf(
//                Movie(
//                    "Titanic",
//                    100f
//                ),
//                Movie(
//                    "slonik",
//                    100f
//                )
//            ),
//            listOf(
//                "boi",
//                "next door"
//            )
//        ),
//        Person(
//            "AiWeeWee",
//            "drug",
//            20,
//            2,
//            1000f,
//            listOf(
//                Movie(
//                    "Anime",
//                    100f
//                ),
//            ),
//            listOf(
//                "kek",
//            )
//        )
//    ),
//    setOf(
//        Movie(
//            "Titanic",
//            100f
//        ),
//        Movie(
//            "slonik",
//            100f
//        )
//    )
//)
//
//
//// Person builders
//
//fun main() {
//
//    println(crowdUsage)
//}