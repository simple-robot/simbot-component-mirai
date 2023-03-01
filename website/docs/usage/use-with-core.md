---
title: 配合核心库使用
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import CodeBlock from '@theme/CodeBlock';


mirai组件中的 `Component` 实现类型为 [`MiraiComponent`](https://docs.simbot.forte.love/components/mirai/simbot-component-mirai-core/love.forte.simbot.component.mirai/-mirai-component/)；

`BotManager` 的实现类型为 [`MiraiBotManager`](https://docs.simbot.forte.love/components/mirai/simbot-component-mirai-core/love.forte.simbot.component.mirai.bot/-mirai-bot-manager)。

### 在Application中安装

在使用 simbot application 时，安装mirai的相关组件与BotManager。此处以 `Simple Application` 为例：

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

### bot注册

<Tabs groupId="code">
<TabItem value="Kotlin" default>

```kotlin
val app = createSimpleApplication {
    useMirai()
}

// 获取MiraiBotManager
app.miraiBots {
    val bot = register(123L, "PASS") {
        // 组件提供的配置

        botConfiguration {
            // mirai原生配置
        }
    }

    bot.start()
}
```


</TabItem>

<TabItem value="Java">

```java
SimpleApplication application = Applications.buildSimbotApplication(Simple.INSTANCE)
        .build((builder, config) -> {
            builder.install(MiraiComponent.Factory, (config1, perceivable) -> Unit.INSTANCE);
            builder.install(MiraiBotManager.Factory, (config1, perceivable) -> Unit.INSTANCE);
        }).createBlocking();

// 寻找并获取MiraiBotManager
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
        break;
    }
}
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
    // 寻找并获取MiraiBotManager
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
            break;
        }
    }
});
```

</TabItem>
</Tabs>



