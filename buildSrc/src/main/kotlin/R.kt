/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 *
 *
 */

import love.forte.gradle.common.core.repository.Repositories
import love.forte.gradle.common.core.repository.SimpleCredentials
import org.slf4j.LoggerFactory


val logger = LoggerFactory.getLogger("Sonatype Userinfo")

private val sonatypeUserInfo by lazy {
    val userInfo = love.forte.gradle.common.publication.sonatypeUserInfoOrNull
    
    if (userInfo == null) {
        logger.warn("sonatype.username or sonatype.password is null, cannot config nexus publishing.")
    }
    
    userInfo
}

private val sonatypeUsername: String? get() = sonatypeUserInfo?.username
private val sonatypePassword: String? get() = sonatypeUserInfo?.password


val ReleaseRepository by lazy {
    
    Repositories.Central.Default.copy(SimpleCredentials(sonatypeUsername, sonatypePassword))
}
val SnapshotRepository by lazy {
    Repositories.Snapshot.Default.copy(SimpleCredentials(sonatypeUsername, sonatypePassword))
}
