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
 *
 */

@file:OptIn(InternalApi::class)

import com.github.ricky12awesome.jss.encodeToSchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.Json
import love.forte.simbot.component.mirai.bot.MiraiBotVerifyInfoConfiguration
import love.forte.simbot.component.mirai.bot.PasswordInfoConfiguration
import love.forte.simbot.component.mirai.internal.InternalApi
import kotlin.test.Test

/*
 *  Copyright (c) 2022 ForteScarlet <ForteScarlet@163.com>
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
 *
 */

class SerTest {
    
    fun schema() {
        val str = Json.encodeToSchema(MiraiBotVerifyInfoConfiguration.serializer(), false)
        println(str)
    }
    
    @Test
    fun serTest() {
        val j1 = """{"pwd":  "literal value"}"""
        val j2 = """{"pwd": {"type": "md5", "md5":  "md5 pwd value"}}"""
        
        println(Json.decodeFromString(Foo.serializer(), j1))
        println(Json.decodeFromString(Foo.serializer(), j2))
        
    }
    
    
}

@Serializable
data class Foo(@Serializable(PwdSerializer::class) val pwd: PasswordInfoConfiguration)

object PwdSerializer : KSerializer<PasswordInfoConfiguration> {
    private fun a() {
        val ser = PolymorphicSerializer(PasswordInfoConfiguration::class)
        
    }
    private val serializer = PasswordInfoConfiguration.serializer()
    private val stringValueDescriptor = PrimitiveSerialDescriptor("passwordLiteralValue", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): PasswordInfoConfiguration {
        decoder.decodeStructure(descriptor) {
            println(this)
        }
        decoder.beginStructure(descriptor)
        
        return PasswordInfoConfiguration.Text("no")
    }
    
    override val descriptor: SerialDescriptor = serializer.descriptor
    
    override fun serialize(encoder: Encoder, value: PasswordInfoConfiguration) {
        serializer.serialize(encoder, value)
    }
}