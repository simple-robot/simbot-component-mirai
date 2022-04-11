# mirai组件扩展：Cat Code兼容

## 使用

### Kotlin

Kotlin中，使用 `catCodeToMessage(...)` 或者 `Neko.toMessage(...)`
将一个猫猫码字符串或者猫猫码对象转化为 `Message` 对象来进行发送。

```kotlin
@Test
fun decode() {
    val at = "[CAT:at,code=123]"
    val message = catCodeToMessage(at)
    assert(message is MessageList) { "Decoded message !is MessageList" }

    message as MessageList

    assert(message.first() == At(123.ID))
}
```

### Java
Java中，相关的操作函数会整合在 `CatCodeMessageUtil` 类中。
```java


```