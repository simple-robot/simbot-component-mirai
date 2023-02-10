---
sidebar_position: 2
title: 安装&使用
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import versionInfo from './versions.json';
import CodeBlock from '@theme/CodeBlock';

:::info 前情提要

你应当已经阅读并了解过了 [simbot3](https://simbot.forte.love) 的基本内容

:::

## 安装

在使用mirai组件时必须**显式指定**simbot核心库的依赖，例如 `simbot-core` 或 `simboot-spring-boot-starter` 等。

下文将以 `simbot-core` 为例，在引入 `simbot-core` 的基础上，使用mirai组件：

<Tabs groupId="use-dependency">
<TabItem value="Gradle Kotlin DSL" default>

<CodeBlock language="kotlin">
{`
implementation("love.forte.simbot:simbot-core:$SIMBOT_VERSION") // simbot核心库
implementation("love.forte.simbot.component:simbot-component-mirai-core:${ versionInfo.core }") // mirai组件
`.trim()}
</CodeBlock>

</TabItem>

<TabItem value="Gradle Groovy">

<CodeBlock language="groovy">
{`
implementation 'love.forte.simbot:simbot-core:$SIMBOT_VERSION' // simbot核心库
implementation 'love.forte.simbot.component:simbot-component-mirai-core:${ versionInfo.core }' // mirai组件
`.trim()}
</CodeBlock>

</TabItem>

<TabItem value="Maven">

<CodeBlock language="xml">
{`
<dependencies>
    <!-- simbot核心库 -->
    <dependency>
        <groupId>love.forte.simbot</groupId>
        <artifactId>simbot-core</artifactId>
        <version>\${simbot.version}</version>
    </dependency>
    <!-- mirai组件 -->
    <dependency>
        <groupId>love.forte.simbot.component</groupId>
        <artifactId>simbot-component-mirai-core</artifactId>
        <version>${ versionInfo.core }</version>
    </dependency>
</dependencies>
`.trim()}
</CodeBlock>

</TabItem>
</Tabs>

## 使用

mirai组件中的 `Component` 实现类型为 [`MiraiComponent`](https://docs.simbot.forte.love/components/mirai/simbot-component-mirai-core/love.forte.simbot.component.mirai/-mirai-component/)；

`BotManager` 的实现类型为 [`MiraiBotManager`](https://docs.simbot.forte.love/components/mirai/simbot-component-mirai-core/love.forte.simbot.component.mirai.bot/-mirai-bot-manager)

### application install

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


### Spring Boot Starter

mirai组件中的 `MiraiComponent` 和 `MiraiBotManager` 均实现并支持了simbot核心库所提供的 `Service Load`。
依据simbot的特性，在 Spring Boot Starter （以下简称starter） 中将会自动加载mirai组件和BotManager。

因此，当使用 starter 时候，只需要书写bot配置文件即可。有关bot配置文件的内容参考章节  [Bot配置文件](bot-config)


