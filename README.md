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

此为 [simbot3](https://github.com/ForteScarlet/simpler-robot/tree/v3-dev) 下基于simbot标准API对 [mirai](https://github.com/mamoe/mirai) 的组件支持。

更多详情请参考 [simbot3文档](https://www.yuque.com/simpler-robot/simpler-robot-doc/mudleb)


### 快速开始
参考文档的 [《快速开始》](https://www.yuque.com/simpler-robot/simpler-robot-doc/fvdmq1)


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
