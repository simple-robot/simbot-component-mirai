---
slug: /
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# 首页

欢迎来到 simbot-mirai 组件文档。

mirai组件是基于 [simbot核心库](https://github.com/simple-robot/simpler-robot) API对 [mirai框架](https://github.com/mamoe/mirai) 的实现。


## 使用

> 首先你应当已经阅读并了解过了 [simbot3](https://simbot.forte.love) 的基本内容

:::tip 别忘了

在使用mirai组件时必须**显式指定**simbot核心库的依赖，例如 `simbot-core` 或 `simboot-spring-boot-starter` 等。

下文将以 `simbot-core` 为例。

:::

<Tabs groupId="use-dependency">
<TabItem value="Gradle Kotlin DSL" default>

```kotlin
implementation("love.forte.simbot:simbot-core:$SIMBOT_VERSION") // simbot核心库
implementation("love.forte.simbot.component:simbot-component-mirai-core:$CMPT_MIRAI_VERSION") // mirai组件
```

</TabItem>

<TabItem value="Gradle Groovy">

```groovy
implementation 'love.forte.simbot:simbot-core:$SIMBOT_VERSION' // simbot核心库
implementation 'love.forte.simbot.component:simbot-component-mirai-core:$CMPT_MIRAI_VERSION' // mirai组件
```

</TabItem>

<TabItem value="Maven">

```xml
<dependencies>
    <!-- simbot核心库 -->z 
    <dependency>
        <groupId>love.forte.simbot</groupId>
        <artifactId>simbot-core</artifactId>
        <version>${simbot.version}</version>
    </dependency>
    <!-- mirai组件 -->
    <dependency>
        <groupId>love.forte.simbot.component</groupId>
        <artifactId>simbot-component-mirai-core</artifactId>
        <version>${simbot-cmpt-mirai.version}</version>
    </dependency>
    
    <!-- ... -->

</dependencies>
```

</TabItem>
</Tabs>
