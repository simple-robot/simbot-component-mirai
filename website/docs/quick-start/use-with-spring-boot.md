---
title: 使用SpringBoot
sidebar_position: 20
pagination_prev: quick-start/index
pagination_next: quick-start/next-step
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import CodeBlock from '@theme/CodeBlock';
import version from '@site/static/version.json'


:::danger TODO

待加工

:::


## 安装

首先准备一个SpringBoot项目。可以考虑前往 [start.spring.io](https://start.spring.io/) 或借助IDE等工具。

:::info 保持住

注意，在使用 Spring Boot 的时候你需要一些能够使程序保持运行的组件，例如通过 `spring-web` 启用一个服务器，否则程序可能会自动终止。 因为 simbot 的 starter 并不提供维持程序运行的能力。

:::

然后额外添加两个我们需要的依赖: 


<Tabs groupId="dept">
<TabItem value="Gradle Kotlin DSL" default>

<CodeBlock language="kotlin" >{`
// 必须显式的引用具体的simboot-core-spring-boot-starter
implementation("love.forte.simbot.boot:simboot-core-spring-boot-starter:$SIMBOT_VERSION") // 版本参考下文所述的 Releases
implementation("love.forte.simbot.component:simbot-component-mirai-core:${version.version}")
`.trim()}</CodeBlock>

</TabItem>
<TabItem value="Gradle Groovy">

<CodeBlock language="gradle" >{`
// 必须显式的引用具体的simboot-core-spring-boot-starter
implementation 'love.forte.simbot.boot:simboot-core-spring-boot-starter:$SIMBOT_VERSION' // 版本参考下文所述的 Releases
implementation 'love.forte.simbot.component:simbot-component-mirai-core:${version.version}'
`.trim()}</CodeBlock>

</TabItem>
<TabItem value="Maven">

<CodeBlock language="xml" >{
`<!-- simboot-core-spring-boot-starter -->
<dependency>
<groupId>love.forte.simbot.boot</groupId>
<artifactId>simboot-core-spring-boot-starter</artifactId>
<!-- 版本参考下文所述的 Releases -->
<version>\${SIMBOT_VERSION}</version>
</dependency>
<dependency>
<groupId>love.forte.simbot.component</groupId>
<artifactId>simbot-component-mirai-core</artifactId>
<version>${version.version}</version>
</dependency>
`.trim()}</CodeBlock>

</TabItem>
</Tabs>


:::tip 核心库版本?

simbot 的 starter 版本 (`simboot-core-spring-boot-starter`) 可前往 [**Releases**](https://github.com/simple-robot/simpler-robot/releases) 查阅。

:::

## 使用

### 启动类

编写一个十分普通的SpringBoot启动类，并在上面追加注解 `@EnableSimbot` 来启用 simbot 。

:::note 天冷了

Spring Boot 的启动类应当拥有**至少一层**的包路径。

:::

<Tabs groupId="code">
<TabItem value="Kotlin" default>

```kotlin title='com.example.App'
@SpringBootApplication
@EnableSimbot // 启用simbot
open class App

fun main(args: Array<String>) {
    runApplication<App>(args = args) // 启动spring boot
}
```

</TabItem>

<TabItem value="Java">

```java title='com.example.App'
@SpringBootApplication
@EnableSimbot
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

</TabItem>
</Tabs>

## 组件加载?

mirai组件中的 `MiraiComponent` 和 `MiraiBotManager` 均实现并支持了 simbot 所约束的 `SPI`。
依据特性，在 Spring Boot Starter 环境中默认的**自动加载**行为会加载mirai组件的这两个实现。

因此默认情况下不需要手动加载它们。

## BOT配置

有关bot配置文件的内容参考章节 [Bot配置文件](../bot-config) 编写bot配置文件，
并将其放置于当前项目**资源目录**下的 `simbot-bots/` 目录中。

```text {6-8}
PROJECT
   |- src
      |- main
          |- java(or kotlin)
          |- resources
                |- simbot-bots <-- 此目录下
                     |- xxx.bot.json <| 每一个文件是一个bot的信息，后缀为 `.bot.json`
                     |- yyy.bot.json <|
```

:::tip 也不一定

bot配置文件的扫描路径是可配置的。

在spring的配置文件中增加 `simbot.bot-configuration-resources` 配置项来指定或修改一个或多个扫描资源。

<Tabs groupId="sb-conf">

<TabItem value="YAML">

```yaml title='application.properties'
simbot:
    bot-configuration-resources:
      - 'classpath:simbot-bots/*.bot*' # 资源路径中的文件
      - 'file:simbot-bots/*.bot*'      # 本地文件系统中的文件
```

</TabItem>

<TabItem value="properties">

```properties title='application.properties'
# 资源路径中的文件
simbot.bot-configuration-resources[0]=classpath:simbot-bots/*.bot*
# 本地文件系统中的文件
simbot.bot-configuration-resources[1]=file:simbot-bots/*.bot*
```

</TabItem>
</Tabs>

:::

## 监听事件

接下来就是逻辑代码所在的地方了，编写一个监听函数并监听一个事件。

此处我们监听 `GroupMessageEvent`，也就是通用的 **群消息事件**。

假设：要求bot必须**被AT**，并且说一句 `你好`，此时bot会引用用户发送的消息并回复 `你也好!`，类似于：

```text
用户:
@BOT 你好

BOT:
> 用户: @BOT 你好
你也好! 
```
<Tabs groupId="code">

<TabItem value="Kotlin">

```kotlin title='com.example.listener.ExampleListener'
import love.forte.simboot.annotation.ContentTrim
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simbot.event.ChannelMessageEvent

@Component
class ExampleListener {
    @Listener
    @Filter(value = "你好", targets = Filter.Targets(atBot = true))
    @ContentTrim // 当匹配被at时，将'at'这个特殊消息移除后，剩余的文本消息大概率存在前后空格，通过此注解在匹配的时候忽略前后空格
    suspend fun onChannelMessage(event: GroupMessageEvent) { // 将要监听的事件类型放在参数里，即代表监听此类型的消息
        // ... 
        event.reply("你也好!")
    }
}


```

</TabItem>

<TabItem value="Java" label="Java Blocking">


```java title='com.example.listener.ExampleListener'
import love.forte.simboot.annotation.ContentTrim
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simbot.event.ChannelMessageEvent
        
@Component
public class ExampleListener {
    @Listener
    @Filter(value = "你好", targets = @Filter.Targets(atBot = true))
    @ContentTrim // 当匹配被at时，将'at'这个特殊消息移除后，剩余的文本消息大概率存在前后空格，通过此注解在匹配的时候忽略前后空格
    public void onChannelMessage(GroupMessageEvent event) { // 将要监听的事件类型放在参数里，即代表监听此类型的消息
        // ... 
        // Java中的阻塞式API
        event.replyBlocking("你也好!");
    }
}
```

</TabItem>

<TabItem value="Java Async">


```java title='com.example.listener.ExampleListener'
import love.forte.simboot.annotation.ContentTrim
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simbot.event.ChannelMessageEvent
        
@Component
public class ExampleListener {
    @Listener
    @Filter(value = "你好", targets = @Filter.Targets(atBot = true))
    @ContentTrim // 当匹配被at时，将'at'这个特殊消息移除后，剩余的文本消息大概率存在前后空格，通过此注解在匹配的时候忽略前后空格
    public CompletableFuture<?> onChannelMessage(GroupMessageEvent event) { // 将要监听的事件类型放在参数里，即代表监听此类型的消息
        // ... 
        // 将 CompletableFuture 作为返回值，simbot会以非阻塞的形式处理它
        return event.replyAsync("你也好!");
    }
}
```

</TabItem>

<TabItem value="Java Reactive">

:::tip 有要求

如果返回值是需要第三方库的响应式类型，那么你的项目环境依赖中必须存在 `Kotlin courotines` 对其的支持库才可使用。
你可以参考文档中  [_响应式的处理结果_](https://simbot.forte.love/docs/basic/event-listener#%E5%8F%AF%E5%93%8D%E5%BA%94%E5%BC%8F%E7%9A%84%E5%A4%84%E7%90%86%E7%BB%93%E6%9E%9C) 的内容。

:::

```java title='com.example.listener.ExampleListener'
import love.forte.simboot.annotation.ContentTrim
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simbot.event.ChannelMessageEvent
        
@Component
public class ExampleListener {
    @Listener
    @Filter(value = "你好", targets = @Filter.Targets(atBot = true))
    @ContentTrim // 当匹配被at时，将'at'这个特殊消息移除后，剩余的文本消息大概率存在前后空格，通过此注解在匹配的时候忽略前后空格
    public Mono<?> onChannelMessage(GroupMessageEvent event) { // 将要监听的事件类型放在参数里，即代表监听此类型的消息
        // ... 
        // 将 Mono 等响应式类型作为返回值，simbot会以非阻塞的形式处理它
        return Mono.fromCompletionStage(event.replyAsync("你也好!"));
    }
}
```

</TabItem>
</Tabs>

## 启动
接下来，启动程序并在你的测试群中@它试试看吧。

当然，如果遇到了预期外的问题也不要慌，积极反馈问题才能使我们变得更好，可以前往 [Issues](https://github.com/simple-robot/simpler-robot/issues) 反馈问题、
[社区](https://github.com/orgs/simple-robot/discussions) 提出疑问。
