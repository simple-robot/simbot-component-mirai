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


plugins {
    id("love.forte.plugin.suspend-transform")
}

suspendTransform {
    includeRuntime = false

    addJvmTransformers(
        // @JvmBlocking
        SuspendTransforms.jvmBlockingTransformer,
        // @JvmAsync
        SuspendTransforms.jvmAsyncTransformer,

        // @JvmSuspendTrans
         SuspendTransforms.jvmSuspendTransTransformerForBlocking,
         SuspendTransforms.jvmSuspendTransTransformerForAsync,

        // @JvmSuspendTransProperty
         SuspendTransforms.jvmSuspendTransPropTransformerForBlocking,
         SuspendTransforms.jvmSuspendTransPropTransformerForAsync
    )
}
