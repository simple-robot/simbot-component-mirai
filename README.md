<div align="center">
<img src=".simbot/logo.png" alt="logo" style="width:230px; height:230px; border-radius:50%; " />
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

更多详情请参考 [simbot3文档](https://www.yuque.com/simpler-robot/simpler-robot-doc/mudleb)


## 文档
你可以参考 [语雀说明文档](https://www.yuque.com/simpler-robot) 的 `simbot3` 部分，或者查看simbot3-mirai组件的 [API Doc](https://simple-robot-library.github.io/simbot3-component-mirai-apiDoc/) .


### 快速开始
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

### 变更mirai版本

有些时候，mirai组件的更新速度可能无法赶上 [mirai](https://github.com/mamoe/mirai) 的版本发布，此时你可以自行更替mirai版本。

**gradle kotlin dsl**

```kotlin
implementation("love.forte.simbot:simbot-core:$SIMBOT_VERSION")
implementation("love.forte.simbot.component:simbot-component-mirai-core:$COMPONENT_VERSION") {
    exclude("net.mamoe", "mirai-core-jvm")
}
// 自行引入
implementation("net.mamoe:mirai-core-jvm:$MIRAI_VERSION")
```
**gradle groovy dsl**

```groovy
implementation 'love.forte.simbot:simbot-core:$SIMBOT_VERSION'
implementation 'love.forte.simbot.component:simbot-component-mirai-core:$COMPONENT_VERSION' {
    exclude group: 'net.mamoe', module: 'mirai-core-jvm'
}

// 自行引入
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
                <artifactId>mirai-core-jvm</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- 自行引入 -->
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
