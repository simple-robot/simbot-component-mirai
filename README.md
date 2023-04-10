<div align="center">
<img src=".simbot/icon.png" alt="logo" style="width:230px; height:230px; border-radius:50%; " />
<h2>
    ~ simple-robot for mirai ~ 
</h2>
<a href="https://github.com/simple-robot/simbot-component-mirai/releases/latest"><img alt="release" src="https://img.shields.io/github/v/release/simple-robot/simbot-component-mirai" /></a>
<a href="https://repo1.maven.org/maven2/love/forte/simbot/component/simbot-component-mirai-core/" target="_blank">
  <img alt="release" src="https://img.shields.io/maven-central/v/love.forte.simbot.component/simbot-component-mirai-core" /></a>
<a href="https://www.yuque.com/simpler-robot/simpler-robot-doc" target="_blank">
  <img alt="doc" src="https://img.shields.io/badge/doc-yuque-brightgreen" /></a>
   <hr>
   <img alt="stars" src="https://img.shields.io/github/stars/simple-robot/simbot-component-mirai" />
   <img alt="forks" src="https://img.shields.io/github/forks/simple-robot/simbot-component-mirai" />
   <img alt="watchers" src="https://img.shields.io/github/watchers/simple-robot/simbot-component-mirai" />
   <img alt="repo size" src="https://img.shields.io/github/repo-size/simple-robot/simbot-component-mirai" />
   <img alt="lines" src="https://img.shields.io/tokei/lines/github/simple-robot/simbot-component-mirai" />
   <img alt="issues" src="https://img.shields.io/github/issues-closed/simple-robot/simbot-component-mirai?color=green" />
   <img alt="last commit" src="https://img.shields.io/github/last-commit/simple-robot/simbot-component-mirai" />
   <a href="./COPYING"><img alt="copying" src="https://img.shields.io/github/license/simple-robot/simbot-component-mirai" /></a>

</div>

<br>
<br>

此为 [simbot3](https://github.com/simple-robot/simpler-robot) 下基于simbot标准API对 [mirai](https://github.com/mamoe/mirai) 的组件支持。


## 文档

Simple Robot 官方文档：[simbot.forte.love](https://simbot.forte.love)

simbot-mirai 组件文档：[component-mirai.simbot.forte.love](https://component-mirai.simbot.forte.love)

API文档：[文档引导站点](https://docs.simbot.forte.love)


## 版本说明

> 其他参考：[命名概述](https://simbot.forte.love/docs/overviews/naming-overview/)

simbot官方组件采用 `X.y.z[.status][-suffix]` 的方式。其中，`X` 代表为当前依赖的 [simbot核心库](https://github.com/simple-robot/simpler-robot) 的 `MAJOR.MINOR`。
例如假若当前依赖的核心库版本为 `v3.0.1`，则 `X` 为 `3.0`。

`y` 和 `z` 以及其他后缀均为常见的语义化版本规则中的对应值，其中 `z` 通常在不包含新功能的修复性更新中增加计数，`y` 则会在包含增加新功能、修改旧功能以及破坏性变更时增加计数，并将 `z` 归零。



## Mirai依赖说明

simbot-mirai组件会不定时的根据 [mirai最后的release版本](https://github.com/mamoe/mirai/releases/latest) 进行更新。

> 目前暂时不会跟进非 `release` 版本，例如 `xxx-RC`。

## 快速开始

参考文档的 [《快速开始》](https://www.yuque.com/simpler-robot/simpler-robot-doc/fvdmq1)

### 使用

注：mirai组件仅编译时依赖 [simbot](https://github.com/simple-robot/simpler-robot) 核心库，因此你必须引入一个具体的核心库版本。

**gradle kotlin dsl**

```kotlin
implementation("love.forte.simbot:simbot-core:$SIMBOT_VERSION")
implementation("love.forte.simbot.component:simbot-component-mirai-core:$COMPONENT_VERSION")
```
**gradle groovy dsl**

```groovy
implementation 'love.forte.simbot:simbot-core:$SIMBOT_VERSION'
implementation 'love.forte.simbot.component:simbot-component-mirai-core:$COMPONENT_VERSION'
```

**maven**

```xml
<dependencies>
    <dependency>
        <groupId>love.forte.simbot.boot</groupId>
        <artifactId>simboot-core-spring-boot-starter</artifactId>
        <version>${simbot.version}</version>
    </dependency>
    <dependency>
        <groupId>love.forte.simbot.component</groupId>
        <artifactId>simbot-component-mirai-core</artifactId>
        <version>${simbot.mirai.version}</version>
    </dependency>
</dependencies>
```

## 其他说明和注意事项

### `cache` 目录的默认行为

从 [#120](https://github.com/simple-robot/simbot-component-mirai/pull/120) 开始，
mirai组件中 `cache` 文件夹的默认行为与 **mirai 原本的**行为不再相同。

当你从其他位置通过各种工具得到了 `device.json` 和 `cache` 文件夹后，你需要稍作调整。

目前组件中针对每个bot的 `cache` 目录规则为 `cache/$CODE` ，也就是 `cache` 
目录下以QQ号为名称的子目录。

举个例子，如果一个bot的qq号为 `123456`，那么它所使用的 cacheDir 为 

```
cache
  \- 123456
```

因此当你希望使用一些外部的 `cache` 目录时，请记得将内容物调整到具体的子目录中。

### 变更mirai版本

有些时候，mirai组件的更新速度可能无法赶上 [mirai](https://github.com/mamoe/mirai) 的版本发布，此时你可以自行更替mirai版本。

**gradle kotlin dsl**

```kotlin
implementation("love.forte.simbot:simbot-core:$SIMBOT_VERSION")
implementation("love.forte.simbot.component:simbot-component-mirai-core:$COMPONENT_VERSION") {
    exclude("net.mamoe") // 排除 groupId = 'net.mamoe' 的所有依赖
}
// 然后自行引入一个新的
implementation("net.mamoe:mirai-core-jvm:$MIRAI_VERSION")
```
**gradle groovy dsl**

```groovy
implementation 'love.forte.simbot:simbot-core:$SIMBOT_VERSION'
implementation 'love.forte.simbot.component:simbot-component-mirai-core:$COMPONENT_VERSION' {
    exclude group: 'net.mamoe' // 排除 groupId = 'net.mamoe' 的所有依赖
}

// 然后自行引入一个新的
implementation 'net.mamoe:mirai-core-jvm:$MIRAI_VERSION'
```

**maven**
```xml
<dependencies>
    <dependency>
        <groupId>love.forte.simbot.boot</groupId>
        <artifactId>simboot-core-spring-boot-starter</artifactId>
        <version>${simbot.version}</version>
    </dependency>
    <dependency>
        <groupId>love.forte.simbot.component</groupId>
        <artifactId>simbot-component-mirai-core</artifactId>
        <version>${simbot.mirai.version}</version>
        <exclusions>
            <exclusion>
                <groupId>net.mamoe</groupId>
                <!--  排除 groupId = 'net.mamoe' 的所有依赖 -->
                <artifactId>*</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- 然后自行引入一个新的 -->
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-jvm</artifactId>
        <version>${mirai.version}</version>
    </dependency>
</dependencies>
```


### 走马观花

```kotlin
// simbot-core / simbot-boot
suspend fun MiraiFriendMessageEvent.onEvent() {
    author().send("Hello World")
}
```


```kotlin
// simbot-boot
@Listener
@Filter("签到")
suspend fun MiraiGroupMessageEvent.onEvent() {
    reply("签到成功")
}
```

```kotlin
@Listener
@Filter("叫我{{name,.+}}")
suspend fun MiraiGroupMessageEvent.onEvent(name: String) {
    group.send(At(author.id) + "好的，以后就叫你$name了".toText())
}
```

# License

```
Copyright (C) 2022-2023 ForteScarlet.

This program is free software: you can redistribute it and/or modify it under the terms 
of the GNU Affero General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program. 
If not, see <https://www.gnu.org/licenses/>.
```


## 衍生软件

直接/间接引用 `simbot-component-mirai` 的项目需要以 `AGPLv3` 协议开源。

本仓库引用开源软件[**mirai**](https://github.com/mamoe/mirai)，衍生软件同样需受其协议约束。
