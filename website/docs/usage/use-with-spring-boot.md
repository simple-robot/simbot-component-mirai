---
title: 配合SpringBoot使用
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import CodeBlock from '@theme/CodeBlock';


### Spring Boot Starter

mirai组件中的 `MiraiComponent` 和 `MiraiBotManager` 均实现并支持了simbot核心库所提供的 `Service Load` 机制。
依据特性，在 Spring Boot Starter（以下简称starter）环境中将会**自动加载**mirai组件和BotManager。

因此，当使用 starter 时候，只需要书写bot配置文件即可。有关bot配置文件的内容参考章节 [Bot配置文件](../bot-config) 编写bot配置文件，
并将其放置于当前项目**资源目录**下的 `simbot-bots/` 目录中。

```text {6-8}
PROJECT
   \-- src
        \-- main
             \-- java(or kotlin)
             \-- resources
                     \ -- simbot-bots <-- 此目录下
                              \-- xxx.bot.json
                              \-- yyy.bot.json
```
:::tip 也不一定

bot配置文件的扫描路径是可配置的。

在spring的配置文件中增加 `simbot.bot-configuration-resources` 配置项来指定或修改一个或多个扫描资源。

:::
