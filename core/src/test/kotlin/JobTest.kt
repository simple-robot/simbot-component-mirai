import kotlinx.coroutines.*

val job = SupervisorJob()
val scope = CoroutineScope(job)


suspend fun main() {

    repeat(1000) {
        scope.async(start = CoroutineStart.LAZY) {
            println("Run deferred.")
            delay(5)
            1
        }
    }

    println(job.children.toList().size)
    println("===")
    delay(200)
    println(job.children.toList().size)
    println("===")
    System.gc()
    System.gc()
    System.gc()
    System.gc()
    delay(200)
    println(job.children.toList().size)
    println("===")



}

