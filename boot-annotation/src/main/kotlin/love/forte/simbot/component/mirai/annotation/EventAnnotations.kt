package love.forte.simbot.component.mirai.annotation

import love.forte.simboot.annotation.Listen
import love.forte.simbot.SimbotDiscreetApi
import love.forte.simbot.component.mirai.event.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.annotation.AnnotationTarget.FUNCTION


@Target(FUNCTION)
@Listen(MiraiFriendMessageEvent::class)
@MustBeDocumented
public annotation class OnMiraiFriendMessage

@Target(FUNCTION)
@Listen(MiraiStrangerMessageEvent::class)
@MustBeDocumented
public annotation class OnMiraiStrangerMessage

@Target(FUNCTION)
@Listen(MiraiGroupMessageEvent::class)
@MustBeDocumented
public annotation class OnMiraiGroupMessage

@Target(FUNCTION)
@Listen(MiraiMemberMessageEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberMessage

@Target(FUNCTION)
@Listen(MiraiFriendRequestEvent::class)
@MustBeDocumented
public annotation class OnMiraiFriendRequest

@Target(FUNCTION)
@Listen(MiraiFriendInputStatusChangedEvent::class)
@MustBeDocumented
public annotation class OnMiraiFriendInputStatusChanged

@Target(FUNCTION)
@Listen(MiraiFriendNickChangedEvent::class)
@MustBeDocumented
public annotation class OnMiraiFriendNickChanged

@Target(FUNCTION)
@Listen(MiraiFriendAvatarChangedEvent::class)
@MustBeDocumented
public annotation class OnMiraiFriendAvatarChanged

@Target(FUNCTION)
@Listen(MiraiFriendDecreaseEvent::class)
@MustBeDocumented
public annotation class OnMiraiFriendDecrease

@Target(FUNCTION)
@Listen(MiraiFriendIncreaseEvent::class)
@MustBeDocumented
public annotation class OnMiraiFriendIncrease

@Target(FUNCTION)
@Listen(MiraiFriendRemarkChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiFriendRemarkChange

@Target(FUNCTION)
@Listen(MiraiBotInvitedJoinGroupRequestEvent::class)
@MustBeDocumented
public annotation class OnMiraiBotInvitedJoinGroupRequest

@Target(FUNCTION)
@Listen(MiraiBotLeaveEvent::class)
@MustBeDocumented
public annotation class OnMiraiBotLeave

@Target(FUNCTION)
@Listen(MiraiBotJoinGroupEvent::class)
@MustBeDocumented
public annotation class OnMiraiBotJoinGroup

@Target(FUNCTION)
@Listen(MiraiBotMuteEvent::class)
@MustBeDocumented
public annotation class OnMiraiBotMute

@Target(FUNCTION)
@Listen(MiraiBotGroupPermissionChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiBotGroupPermissionChange

@Target(FUNCTION)
@Listen(MiraiBotUnmuteEvent::class)
@MustBeDocumented
public annotation class OnMiraiBotUnmute

@Target(FUNCTION)
@Listen(MiraiGroupNameChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiGroupNameChange

@Target(FUNCTION)
@Listen(MiraiGroupEntranceAnnouncementChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiGroupEntranceAnnouncementChange

@Target(FUNCTION)
@Listen(MiraiGroupMuteAllEvent::class)
@MustBeDocumented
public annotation class OnMiraiGroupMuteAll

@Target(FUNCTION)
@Listen(MiraiGroupAllowAnonymousChatEvent::class)
@MustBeDocumented
public annotation class OnMiraiGroupAllowAnonymousChat

@Target(FUNCTION)
@Listen(MiraiGroupAllowConfessTalkEvent::class)
@MustBeDocumented
public annotation class OnMiraiGroupAllowConfessTalk

@Target(FUNCTION)
@Listen(MiraiGroupAllowMemberInviteEvent::class)
@MustBeDocumented
public annotation class OnMiraiGroupAllowMemberInvite

@Target(FUNCTION)
@Listen(MiraiGroupTalkativeChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiGroupTalkativeChange

@MiraiExperimentalApi
@Target(FUNCTION)
@Listen(MiraiMemberHonorChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberHonorChange

@Target(FUNCTION)
@Listen(MiraiMemberUnmuteEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberUnmute

@Target(FUNCTION)
@Listen(MiraiMemberMuteEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberMute

@Target(FUNCTION)
@Listen(MiraiMemberPermissionChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberPermissionChange

@Target(FUNCTION)
@Listen(MiraiMemberSpecialTitleChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberSpecialTitleChange

@Target(FUNCTION)
@Listen(MiraiMemberCardChangeEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberCardChange

@Target(FUNCTION)
@Listen(MiraiMemberJoinRequestEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberJoinRequest

@Target(FUNCTION)
@Listen(MiraiMemberLeaveEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberLeave

@Target(FUNCTION)
@Listen(MiraiMemberJoinEvent::class)
@MustBeDocumented
public annotation class OnMiraiMemberJoin

@SimbotDiscreetApi
@Target(FUNCTION)
@Listen(UnsupportedMiraiEvent::class)
@MustBeDocumented
public annotation class OnUnsupportedMirai