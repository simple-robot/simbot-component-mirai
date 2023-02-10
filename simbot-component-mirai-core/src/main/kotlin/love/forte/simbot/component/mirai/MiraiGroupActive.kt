/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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

package love.forte.simbot.component.mirai

import kotlinx.coroutines.flow.Flow
import love.forte.simbot.Api4J
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.contact.active.ActiveChart
import net.mamoe.mirai.contact.active.ActiveRecord
import net.mamoe.mirai.contact.active.GroupActive
import java.util.stream.Stream


/**
 * 针对 [GroupActive] 的封装类型，通过 [MiraiGroup.active] 获取。
 *
 * 有关各API或属性的详细描述请参考 [GroupActive]。
 *
 * > 表示一个群活跃度管理.
 *
 * @see GroupActive
 * @author ForteScarlet
 */
@JST
@MiraiMappingType(GroupActive::class)
public interface MiraiGroupActive {
    
    /**
     * 得到当前实例所代表的具体的 [GroupActive].
     */
    public val originalGroupActive: GroupActive
    
    /**
     * 得到 [GroupActive] 中的 [ActiveRecord] 数据流。
     *
     * Java 请使用 [activeRecordStream].
     *
     * @see GroupActive
     */
    @get:JvmSynthetic
    public val activeRecordFlow: Flow<ActiveRecord> get() = originalGroupActive.asFlow()
    
    
    /**
     * 得到 [GroupActive] 中的 [ActiveRecord] 数据流。
     *
     * @see GroupActive
     */
    @Api4J
    public val activeRecordStream: Stream<ActiveRecord> get() = originalGroupActive.asStream()
    
    
    /**
     * 通过 [activeRecordFlow] 获取数据流并阻塞地收集为列表。
     *
     * @see GroupActive
     */
    @Api4J
    public val activeRecordList: List<ActiveRecord> get() = runInBlocking { originalGroupActive.toList() }
    
    
    /**
     * 是否在群聊中显示荣誉
     * @see GroupActive.isHonorVisible
     */
    public val isHonorVisible: Boolean get() = originalGroupActive.isHonorVisible
    
    /**
     * 设置是否在群聊中显示荣誉
     * @see GroupActive.setHonorVisible
     */
    public suspend fun setHonorVisible(newValue: Boolean) {
        originalGroupActive.setHonorVisible(newValue)
    }
    
    /**
     * 是否在群聊中显示头衔
     * @see GroupActive.isTitleVisible
     */
    public val isTitleVisible: Boolean get() = originalGroupActive.isTitleVisible
    
    /**
     * 设置是否在群聊中显示头衔。操作成功时会同时刷新等级头衔信息。
     * @see GroupActive.setTitleVisible
     */
    public suspend fun setTitleVisible(newValue: Boolean) {
        originalGroupActive.setTitleVisible(newValue)
    }
    
    /**
     * 是否在群聊中显示活跃度
     * @see GroupActive.isTemperatureVisible
     */
    public val isTemperatureVisible: Boolean get() = originalGroupActive.isTemperatureVisible
    
    /**
     * 设置是否在群聊中显示活跃度。操作成功时会同时刷新等级头衔信息。
     * @see GroupActive.setTemperatureVisible
     */
    public suspend fun setTemperatureVisible(newValue: Boolean) {
        originalGroupActive.setTemperatureVisible(newValue)
    }
    
    /**
     * 等级头衔列表，键是等级，值是头衔
     *
     * @see GroupActive.rankTitles
     */
    public val rankTitles: Map<Int, String> get() = originalGroupActive.rankTitles
    
    /**
     * 设置等级头衔列表，键是等级，值是头衔。操作成功时会同时刷新等级头衔信息。
     * @see GroupActive.setRankTitles
     */
    public suspend fun setRankTitles(newValue: Map<Int, String>) {
        originalGroupActive.setRankTitles(newValue)
    }
    
    /**
     * 活跃度头衔列表，键是等级，值是头衔。操作成功时会同时刷新活跃度头衔信息。
     * @see GroupActive.temperatureTitles
     */
    public val temperatureTitles: Map<Int, String> get() = originalGroupActive.temperatureTitles
    
    /**
     * 设置活跃度头衔列表，键是等级，值是头衔。操作成功时会同时刷新活跃度头衔信息。
     * @see GroupActive.setTemperatureTitles
     */
    public suspend fun setTemperatureTitles(newValue: Map<Int, String>) {
        originalGroupActive.setTemperatureTitles(newValue)
    }
    
    /**
     * 刷新成员活跃度 （[MiraiMember.active]） 中的属性, 具体说明请参考 [GroupActive.refresh]。
     * @see GroupActive.refresh
     */
    public suspend fun refresh() {
        originalGroupActive.refresh()
    }
    
    /**
     * 获取活跃度图表数据, 直接得到原生的mirai类型 [ActiveChart]。
     * @see GroupActive.queryChart
     */
    public suspend fun queryChart(): ActiveChart = originalGroupActive.queryChart()
}
