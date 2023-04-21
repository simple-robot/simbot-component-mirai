---
title: 使用simbot核心库
sidebar_position: 10
pagination_prev: quick-start/index
pagination_next: quick-start/next-step
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import CodeBlock from '@theme/CodeBlock';
import version from '@site/static/version.json'

mirai组件中的 `Component` 实现类型为 [`MiraiComponent`](https://docs.simbot.forte.love/components/mirai/simbot-component-mirai-core/love.forte.simbot.component.mirai/-mirai-component/)；

`BotManager` 的实现类型为 [`MiraiBotManager`](https://docs.simbot.forte.love/components/mirai/simbot-component-mirai-core/love.forte.simbot.component.mirai.bot/-mirai-bot-manager)。

## 安装

<Tabs groupId="dept">
<TabItem value="Gradle Kotlin DSL" default>

<CodeBlock language="kotlin" >{`
// 必须显式的引用具体的simbot-core
implementation("love.forte.simbot:simbot-core:$SIMBOT_VERSION") // 版本参考下文所述的参考连接
implementation("love.forte.simbot.component:simbot-component-mirai-core:${version.version}")
`.trim()}</CodeBlock>

</TabItem>
<TabItem value="Gradle Groovy">

<CodeBlock language="gradle" >{`
// 必须显式的引用具体的simbot-core
implementation 'love.forte.simbot:simbot-core:$SIMBOT_VERSION' // 版本参考下文所述的参考连接
implementation 'love.forte.simbot.component:simbot-component-mirai-core:${version.version}'
`.trim()}</CodeBlock>

</TabItem>
<TabItem value="Maven">

<CodeBlock language="xml" >{
`<!-- 必须显式的引用具体的simbot-core -->
<dependency>
    <groupId>love.forte.simbot</groupId>
    <artifactId>simbot-core</artifactId>
    <!-- 版本参考下文所述的参考连接 -->
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

simbot核心库的版本 (`simbot-core`) 可前往 [**Releases**](https://github.com/simple-robot/simpler-robot/releases) 查阅。

:::

## 使用

在 simbot application 中安装mirai的相关组件与 `BotManager`。此处以 `Simple Application` 为例：

<Tabs groupId="code">
<TabItem value="Kotlin" default>

**直接安装**

```kotlin
val app = createSimpleApplication {
    install(MiraiComponent)
    install(MiraiBotManager)
}
```

**使用扩展**

```kotlin
val app = createSimpleApplication {
    useMiraiComponent()
    useMiraiBotManager()
}
```

或

```kotlin
 val app = createSimpleApplication {
    useMirai()
}
```


</TabItem>

<TabItem value="Java">

```java
SimpleApplication application = Applications.buildSimbotApplication(Simple.INSTANCE)
        .build((builder, config) -> {
            // 安装mirai组件
            builder.install(MiraiComponent.Factory, (config1, perceivable) -> Unit.INSTANCE);
            builder.install(MiraiBotManager.Factory, (config1, perceivable) -> Unit.INSTANCE);
        }).createBlocking();
```

</TabItem>
<TabItem value="Java Async">

```java
CompletableFuture<? extends SimpleApplication> applicationAsync = Applications.buildSimbotApplication(Simple.INSTANCE)
        .build((builder, config) -> {
            // 安装mirai组件
            builder.install(MiraiComponent.Factory, (config1, perceivable) -> Unit.INSTANCE);
            builder.install(MiraiBotManager.Factory, (config1, perceivable) -> Unit.INSTANCE);
        }).createAsync();
```

</TabItem>
</Tabs>

## 事件监听

<Tabs groupId="code">
<TabItem value="Kotlin" default>

```kotlin
val app = createSimpleApplication {
    useMirai()
}

// 使用 eventListenerManager 并注解监听函数
app.eventListenerManager.listeners {
    // 例1：监听一个通用的群消息事件
    // highlight-start
    GroupMessageEvent { event ->
        // 如果匹配成功，回复 "你也好"
        event.reply("你也好")
    } onMatch { event ->
        // 事件匹配要求: 消息的文本为 "你好"
        event.messageContent.plainText.trim() == "你好"
    }
    // highlight-end

    // 例2：监听一个mirai组件具体的群消息事件
    // MiraiGroupMessageEvent 类型本质上就是实现 GroupMessageEvent
    // 例2使用另一种监听函数注册方式
    // highlight-start
    listen(MiraiGroupMessageEvent) {
        // 事件匹配要求: 消息的文本为 "你好"
        match { event -> event.messageContent.plainText.trim() == "你好" }
        // 如果匹配成功，引用回复 "你也好"
        process { event -> event.reply("你也好") }
    }
    // highlight-end
}
```


</TabItem>

<TabItem value="Java">

```java
SimpleApplication application = Applications.buildSimbotApplication(Simple.INSTANCE).build((builder, config) -> {
    builder.install(MiraiComponent.Factory, (__, ___) -> Unit.INSTANCE);  // 忽略配置
    builder.install(MiraiBotManager.Factory, (__, ___) -> Unit.INSTANCE); // 忽略配置
}).createBlocking();

// 注册监听函数，首先构建所需的监听函数实例
// 监听一个通用的群消息事件
// highlight-start
EventListener listener = SimpleListeners.listener(GroupMessageEvent.Key,
        // 事件匹配要求: 消息的文本为 "你好"
        (context, event) -> event.getMessageContent().getPlainText().trim().equals("你好"),
        // 如果匹配成功，回复 "你也好"
        (context, event) -> {
            event.replyBlocking("你也好");
        });
// highlight-end

// 注册这个监听函数
// highlight-start
application.getEventListenerManager().register(listener);
// highlight-end
```

</TabItem>
<TabItem value="Java Async">

```java
CompletableFuture<? extends SimpleApplication> applicationAsync = Applications.buildSimbotApplication(Simple.INSTANCE)
        .build((builder, config) -> {
            builder.install(MiraiComponent.Factory, (config1, perceivable) -> Unit.INSTANCE);
            builder.install(MiraiBotManager.Factory, (config1, perceivable) -> Unit.INSTANCE);
        }).createAsync();

// 注册监听函数，首先构建所需的监听函数实例
// 监听一个通用的群消息事件
// highlight-start
EventListener listener = SimpleListeners.listener(GroupMessageEvent.Key,
        // 事件匹配要求: 消息的文本为 "你好"
        (context, event) -> event.getMessageContent().getPlainText().trim().equals("你好"),
        // 如果匹配成功，回复 "你也好"
        (context, event) -> {
            CompletableFuture<? extends MessageReceipt> replyFuture = event.replyAsync("你也好");
            // 将异步结果 future 通过 EventResult.of 返回，事件处理器会挂起并对其进行处理
            return EventResult.of(replyFuture);
        });
// highlight-end

// 在application准备结束后注册监听函数
applicationAsync.thenCompose(application -> {
    // 注册准备的监听函数
    // highlight-start
    application.getEventListenerManager().register(listener);
    // highlight-end

    // 其他操作结束后，返回 application 的future
    return application.asFuture();
}).join(); // 直到 application 结束/被关闭
```

</TabItem>
</Tabs>


## bot注册

安装完组件与 `MiraiBotManager` 之后，即可注册bot并启用它们。

:::tip 先后

比较建议在监听函数注册结束后再注册 bot，这样当监听函数中存在例如 `BotRegisteredEvent` 等与 bot 相关的事件时就可以正常生效。

:::

<Tabs groupId="code">
<TabItem value="Kotlin" default>

```kotlin
val app = createSimpleApplication {
    useMirai()
}

// 注册监听函数，省略...

// 注册mirai的bot
// 直接使用mirai所提供的扩展函数 miraiBots
// highlight-start
app.miraiBots { // this: MiraiBotManager
    // 通过账号密码注册
    val bot = register(123L, "PASS") { // this: MiraiBotConfiguration
        // mirai组件提供的配置

        // 可以通过 botConfiguration 来直接配置 mirai bot 的原生配置
        botConfiguration { // this: BotConfiguration
            // ...
        }
    }
    // 启用bot
    bot.start()
}
// highlight-end
```


</TabItem>

<TabItem value="Java">

```java
SimpleApplication application = Applications.buildSimbotApplication(Simple.INSTANCE)
        .build((builder, config) -> {
            builder.install(MiraiComponent.Factory, (config1, perceivable) -> Unit.INSTANCE);
            builder.install(MiraiBotManager.Factory, (config1, perceivable) -> Unit.INSTANCE);
        }).createBlocking();

// 注册mirai的bot
// 寻找并获取MiraiBotManager
// highlight-start
BotManagers botManagers = application.getBotManagers();
for (BotManager<?> botManager : botManagers) {
    if (botManager instanceof MiraiBotManager miraiBotManager) {
        MiraiBot bot = miraiBotManager.register(123L, "PASSWORD", configuration -> {
            // 组件配置
            configuration.botConfiguration(originalMiraiConfiguration -> {
                // mirai原生配置
            });
        });
        
        bot.startBlocking();
        break; // 通常情况下只需要获取第一个，因此当匹配后终止寻找
    }
}
// highlight-end
```

</TabItem>
<TabItem value="Java Async">

```java
CompletableFuture<? extends SimpleApplication> applicationAsync = Applications.buildSimbotApplication(Simple.INSTANCE)
        .build((builder, config) -> {
            builder.install(MiraiComponent.Factory, (config1, perceivable) -> Unit.INSTANCE);
            builder.install(MiraiBotManager.Factory, (config1, perceivable) -> Unit.INSTANCE);
        }).createAsync();

applicationAsync.thenAccept(application -> {

    // 注册mirai的bot
    // 寻找并获取MiraiBotManager
    // highlight-start
    BotManagers botManagers = application.getBotManagers();
    for (BotManager<?> botManager : botManagers) {
        if (botManager instanceof MiraiBotManager miraiBotManager) {
            MiraiBot bot = miraiBotManager.register(123L, "PASSWORD", configuration -> {
                // 组件配置
                configuration.botConfiguration(originalMiraiConfiguration -> {
                    // mirai原生配置
                });
            });
            bot.startAsync();
            break; // 通常情况下只需要获取第一个，因此当匹配后终止寻找
        }
    }
    // highlight-end
});
```

</TabItem>
</Tabs>

## 信息获取

很多信息的获取也都是从bot开始的（除事件以外），比如获取群列表、获取频道列表等等。

<Tabs groupId="code">
<TabItem value="Kotlin" default>

```kotlin
val bot: MiraiBot = ... // 注册并启动后的bot

// 获取所有的群列表
bot.groups.collect { ... }
// 尝试获取指定群号的群
val group: MiraiGroup? = bot.group(123.ID)

// 获取群中所有的成员列表
group?.members?.collect { ... }
// 尝试获取群中成员
val member: MiraiMember? = group?.member(666.ID)
```


</TabItem>

<TabItem value="Java">

```java
MiraiBot bot = ...; // 注册并启动后的bot
// 获取所有的群列表
bot.getGroups().collect(group -> { ... });
// 尝试获取指定群号的群
MiraiGroup group = bot.getGroup(Identifies.ID(123));
// Note: group 是 nullable 的, 真正使用的时候注意处理

// 获取群中所有的成员列表
group.getMembers().collect(member -> { ... });
// 尝试获取群中成员
MiraiMember member = group.getMember(Identifies.ID(666));
// Note: member 是 nullable 的, 真正使用的时候注意处理
```

</TabItem>
<TabItem value="Java Async">

```java
MiraiBot bot = ...; // 注册并启动后的bot
// 获取所有的群列表
bot.getGroups().collectAsync(group -> { ... });

// 尝试获取指定群号的群
bot.getGroupAsync(Identifies.ID(123)).thenApply(group -> {
    // Note: group 是 nullable 的, 真正使用的时候注意处理
    return group;
}).thenCompose(group -> {
    // 获取群中所有的成员列表
    group.getMembers().collectAsync(member -> { ...});
    // 尝试获取群中成员
    return group.getMemberAsync(Identifies.ID(666))
}).thenAccept(member -> {
    // Note: member 是 nullable 的, 真正使用的时候注意处理
});
```

</TabItem>
</Tabs>


