import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun main() {



    val value = coroutineScope {
        launch {
            delay(500)
            println("err!")
            throw IllegalStateException()
        }
        println("return 1")

        1
    }
    println("down.")
    println("value: $value")

    delay(1200)
}