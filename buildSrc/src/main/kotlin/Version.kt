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

// https://semver.org/lang/zh-CN/

/**
 * **P**roject **V**ersion。
 */
@Suppress("SpellCheckingInspection")
data class Version(
    /**
     * 主版号
     */
    val major: String,
    /**
     * 次版号
     */
    val minor: Int,
    /**
     * 修订号
     */
    val patch: Int,
    
    /**
     * 状态号。状态号会追加在 [major].[minor].[patch] 之后，由 PVS 拼接，
     * 变为 [major].[minor].[patch].[VersionStatus.status].[VersionStatus.minor].[VersionStatus.patch].
     *
     * 例如：
     * ```
     * 3.0.0.preview.0.1
     * ```
     *
     */
    val status: VersionStatus? = null,
    
    /**
     * 是否快照。如果是，将会在版本号结尾处拼接 [-SNAPSHOT][SNAPSHOT_SUFFIX]。
     */
    val isSnapshot: Boolean = false,
) {
    companion object {
        const val SNAPSHOT_SUFFIX = "-SNAPSHOT"
    }
    
    /**
     * 没有任何后缀的版本号。
     */
    val standardVersion: String = "$major.$minor.$patch"
    
    
    /**
     * 完整的版本号。
     */
    fun fullVersion(checkSnapshot: Boolean): String {
        return buildString {
            append(major).append('.').append(minor).append('.').append(patch)
            if (status != null) {
                status.joinToVersion(this)
            }
            if (checkSnapshot && isSnapshot) {
                append(SNAPSHOT_SUFFIX)
            }
        }
    }
    
}

/**
 * **P**roject **V**ersion **S**tatus.
 */
@Suppress("SpellCheckingInspection")
data class VersionStatus(
    /**
     * 状态名称。例如 [PREVIEW_STATUS] 、 [BETA_STATUS]。
     */
    val status: String,
    
    /**
     * 版本到状态之间的连接符。比如 `1.0-beta` 中间的 `-`。
     */
    val joiner: String,
    
    /**
     * 次版号。不为null时才会被拼接。应当 >= 0。
     */
    val minor: Int?,
    
    /**
     * 修订号。只有 [minor] 不为null的时候才会被检测。应当 >= 0。
     */
    val patch: Int?,
    
    /**
     * 版本额外后缀，例如 `-M1`、`-RC`。
     */
    val suffix: String?,
) {
    
    fun joinToVersion(builder: StringBuilder) {
        builder.apply {
            append(joiner).append(status)
            if (minor != null) {
                append('.').append(minor)
                if (patch != null) {
                    append('.').append(patch)
                }
            }
            if (suffix != null) {
                append(suffix)
            }
        }
    }
    
    companion object {
        const val PREVIEW_STATUS = "preview"
        const val BETA_STATUS = "beta"
        
    }
}


internal fun VersionStatus.Companion.preview(minor: Int?, patch: Int?, suffix: String? = null) =
    VersionStatus(PREVIEW_STATUS, ".", minor, patch, suffix)

internal fun VersionStatus.Companion.beta(minor: Int?, patch: Int?, suffix: String? = null) =
    VersionStatus(BETA_STATUS, "-", minor, patch, suffix)
