# Module simbot-component-mirai-extra-catcode

mirai组件扩展：CatCode兼容

## 使用

**Gradle Kotlin DSL**
```kotlin
implementation("love.forte.simbot.component:simbot-component-mirai-extra-catcode:$version")
```

**Gradle Groovy**
```groovy
implementation 'love.forte.simbot.component:simbot-component-mirai-extra-catcode:$version'
```

**Maven**
```xml
<dependency>
    <groupId>love.forte.simbot.component</groupId>
    <artifactId>simbot-component-mirai-extra-catcode</artifactId>
    <version>${version}</version>
</dependency>
```



## 示例
### 1.Kotlin

Kotlin中，使用 `catCodeToMessage(...)` 或者 `Neko.toMessage(...)`
将一个猫猫码字符串或者猫猫码对象转化为 `Message` 对象来进行发送。

1.1: 解析猫猫码为`Message`对象
```kotlin
val at = "[CAT:at,code=123]"
val message: Message = catCodeToMessage(at)
```

1.2: 解析`Neko`为`Message.Element<*>`对象
```kotlin
val at = CatCodeUtil.nekoTemplate.at(123)
val message: Message.Element<*> = at.toMessage()
```

1.3: 将Mirai的 `SingleMessage` 转化为 `Neko`
```kotlin
val at = net.mamoe.mirai.message.data.At(123)
val neko = at.toNeko()
```

### 2.Java
Java中，相关的操作函数会整合在 `CatCodeMessageUtil` 类中。

2.1: 解析猫猫码为`Message`对象
```java 
final String cat =  "[CAT:at,code=123]";
final Message message = CatCodeMessageUtil.catCodeToMessage(cat);
```

2.2: 解析`Neko`为`Message.Element<*>`对象
```java 
final Neko cat = CatCodeUtil.INSTANCE.getNekoTemplate().at(123);
final Message.Element<?> message = CatCodeMessageUtil.toMessage(cat);
```

2.3: 将Mirai的 `SingleMessage` 转化为 `Neko`
```java 
final At at = new At(123);
final Neko neko = CatCodeMessageUtil.toNeko(at);
```
