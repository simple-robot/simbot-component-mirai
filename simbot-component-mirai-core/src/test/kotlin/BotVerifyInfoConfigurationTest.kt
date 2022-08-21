/*
 *  Copyright (c) 2022-2022 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU 通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU 通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU 通用公共许可证的复本。如果没有，请看:
 *  https://www.gnu.org/licenses
 *  https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *  https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 *
 */

import kotlinx.serialization.json.Json
import love.forte.simbot.component.mirai.CustomPropertiesMiraiRecallMessageCacheStrategy
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.bot.PasswordInfoConfiguration
import love.forte.simbot.component.mirai.bot.RecallMessageCacheStrategyConfiguration
import love.forte.simbot.utils.md5
import love.forte.simbot.utils.toHex
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.MessageChain
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertTrue


/**
 *
 * @author ForteScarlet
 */
class BotVerifyInfoConfigurationTest {
    private inline fun decodeTest(target: String, block: () -> Boolean) {
        assertTrue("$target decode test failure", block)
    }
    
    @Test
    fun passwordInfoConfigurationSerializerTest() {
        fun decodeFromJson(@Language("json") json: String): PasswordInfoConfiguration {
            return Json.decodeFromString(PasswordInfoConfiguration.serializer(), json)
        }
        
        decodeTest("PasswordInfoConfiguration.Text") {
            PasswordInfoConfiguration.text("text") == decodeFromJson(
                """{"type": "text", "text": "text"}"""
            )
        }
        
        decodeTest("PasswordInfoConfiguration.MD5Text") {
            val md5 = md5 { update("forte".toByteArray()) }.toHex()
            PasswordInfoConfiguration.md5(md5) == decodeFromJson(
                """{"type": "md5_text", "md5": "$md5"}"""
            )
        }
        
        decodeTest("PasswordInfoConfiguration.MD5Bytes") {
            val md5 = md5 { update("forte".toByteArray()) }
            PasswordInfoConfiguration.md5(md5) == decodeFromJson(
                """{"type": "md5_bytes", "md5": ${md5.joinToString(",", "[", "]")}}"""
            )
        }
        
        decodeTest("PasswordInfoConfiguration.env.text") {
            PasswordInfoConfiguration.env("prop", "env").text() == decodeFromJson(
                """{"type": "env_text", "prop": "prop", "env": "env"}"""
            )
        }
        
        decodeTest("PasswordInfoConfiguration.env.md5") {
            PasswordInfoConfiguration.env(null, "env").md5() == decodeFromJson(
                """{"type": "env_md5_text", "env": "env"}"""
            )
        }
        
        
    }
    
    @Test
    fun deviceInfoConfigurationSerializerTest() {
        fun decodeFromJson(@Language("json") json: String): DeviceInfoConfiguration {
            return Json.decodeFromString(DeviceInfoConfiguration.serializer(), json)
        }
        
        decodeTest("DeviceInfoConfiguration.auto(baseDir = null)") {
            DeviceInfoConfiguration.auto(baseDir = null) == decodeFromJson(
                """{"type": "auto"}"""
            )
        }
        
        decodeTest("DeviceInfoConfiguration.auto(baseDir = \"...\")") {
            val dir = "/foo/bar"
            DeviceInfoConfiguration.auto(baseDir = dir) == decodeFromJson(
                """{"type": "${DeviceInfoConfiguration.Auto.TYPE}", "baseDir": "$dir"}"""
            )
        }
        
        decodeTest("DeviceInfoConfiguration.random()") {
            DeviceInfoConfiguration.random() == decodeFromJson(
                """{"type": "${DeviceInfoConfiguration.OriginalRandom.TYPE}"}"""
            )
        }
        
        decodeTest("DeviceInfoConfiguration.simbotRandom()") {
            DeviceInfoConfiguration.simbotRandom() == decodeFromJson(
                """{"type": "${DeviceInfoConfiguration.SimbotRandom.TYPE}"}"""
            )
        }
        
        decodeTest("DeviceInfoConfiguration.resource(\"foo\", \"bar\")") {
            DeviceInfoConfiguration.resource("foo", "bar").also(::println) == decodeFromJson(
                """{"type": "${DeviceInfoConfiguration.Resource.TYPE}", "paths": ["foo", "bar"]}"""
            )
        }
    }
    
    @Test
    fun recallMessageCacheStrategyConfigurationSerializerTest() {
        fun decodeFromJson(@Language("json") json: String): RecallMessageCacheStrategyConfiguration {
            return Json.decodeFromString(RecallMessageCacheStrategyConfiguration.serializer(), json)
        }
        
        decodeTest("""RecallMessageCacheStrategyConfiguration.Invalid""") {
            RecallMessageCacheStrategyConfiguration.invalid() == decodeFromJson(
                """{"type": "invalid"}"""
            )
        }
        
        decodeTest("""RecallMessageCacheStrategyConfiguration.MemoryLru""") {
            RecallMessageCacheStrategyConfiguration.memoryLru(
                loadFactor = 0.766F,
                groupMaxSize = 1536
            ).also(::println) == decodeFromJson(
                """{"type": "memory_lru", "loadFactor": 0.766, "groupMaxSize": 1536}"""
            )
        }
        
        
        decodeTest("""RecallMessageCacheStrategyConfiguration.CustomProperties""") {
            RecallMessageCacheStrategyConfiguration.customProperties(
                className = "TestCustomProperties",
                properties = mapOf("foo" to "foo", "tar" to "bar")
            ).also(::println) == decodeFromJson(
                """
                    {
                      "type": "custom_properties",
                      "className": "TestCustomProperties",
                      "properties": {
                        "foo": "foo",
                        "tar": "bar"
                      }
                    }
                """.trimIndent()
            )
        }
        
        
    }
    
}


class TestCustomProperties : CustomPropertiesMiraiRecallMessageCacheStrategy() {
    override fun cacheGroupMessageEvent(bot: MiraiBot, event: GroupMessageEvent) {
        TODO("Not yet implemented")
    }
    
    override fun cacheFriendMessageEvent(bot: MiraiBot, event: FriendMessageEvent) {
        TODO("Not yet implemented")
    }
    
    override fun getGroupMessageCache(bot: MiraiBot, event: MessageRecallEvent.GroupRecall): MessageChain? {
        TODO("Not yet implemented")
    }
    
    override fun getFriendMessageCache(bot: MiraiBot, event: MessageRecallEvent.FriendRecall): MessageChain? {
        TODO("Not yet implemented")
    }
    
    override fun invokeOnBotCompletion(bot: MiraiBot, cause: Throwable?) {
        TODO("Not yet implemented")
    }
}