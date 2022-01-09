import kotlinx.coroutines.SupervisorJob

suspend fun main() {
    val job = SupervisorJob()

    val job1 = SupervisorJob(job)


    job1.cancel()
    job1.join()

    println(job1.isCancelled)
    println(job.isCancelled)


}