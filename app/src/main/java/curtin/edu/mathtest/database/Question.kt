package curtin.edu.mathtest.database

data class Question(
    val question : String,
    val responses : List<Int>,
    val answer : Int,
    val allowedTime : Int)
