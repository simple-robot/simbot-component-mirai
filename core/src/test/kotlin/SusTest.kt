import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

interface Element

interface MyElement : Element

@SerialName("myEleImpl")
@Serializable
data class MyElementImpl(val name: String) : MyElement

fun main() {
    val j = Json {
        serializersModule = SerializersModule {
            polymorphic(Element::class) {
                polymorphic(MyElement::class) {
                    subclass(MyElementImpl.serializer())
                }
                subclass(MyElementImpl.serializer())
            }

        }
    }

    val list = listOf<Element>(
        MyElementImpl("forte"),
        MyElementImpl("forli")
    )

    val jsonStr = j.encodeToString(list)
    println(jsonStr)

    val list2 = j.decodeFromString<List<MyElement>>(jsonStr)
    println(list2)
}