/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  This file is part of simbot-component-mirai.
 *
 *  simbot-component-mirai is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  simbot-component-mirai is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with simbot-component-mirai. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.MiraiBotRegisteredEvent
import love.forte.simbot.component.mirai.event.MiraiBotStartedEvent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.randomID

internal class MiraiBotRegisteredEventImpl(override val bot: MiraiBotImpl) : MiraiBotRegisteredEvent() {
    override val timestamp: Timestamp = Timestamp.now()
    override val id: ID = randomID()
}

internal class MiraiBotStartedEventImpl(override val bot: MiraiBotImpl) : MiraiBotStartedEvent() {
    override val timestamp: Timestamp = Timestamp.now()
    override val id: ID = randomID()
}
