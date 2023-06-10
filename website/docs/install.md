---
sidebar_position: 2
title: 安装
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import versionInfo from '/versions.json';
import CodeBlock from '@theme/CodeBlock';

:::info 前情提要

你应当已经阅读并了解过了 [simbot3](https://simbot.forte.love) 的基本内容

:::


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

:::tip 孤高

simbot的**组件依赖**通常不会因是否使用spring-boot-starter而变化。

:::
