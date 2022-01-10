import love.forte.simbot.resources.toResource
import java.net.URL

fun main() {

    val url = URL("http://forte.love")

    url.toResource().use { r ->
        r.openStream().reader().use {
            println(it.readText())
        }


    }



}