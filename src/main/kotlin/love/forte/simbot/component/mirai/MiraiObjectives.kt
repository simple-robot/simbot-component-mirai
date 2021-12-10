package love.forte.simbot.component.mirai



public typealias MUser = net.mamoe.mirai.contact.User
public typealias SUser = love.forte.simbot.definition.User

public typealias MContact = net.mamoe.mirai.contact.Contact
public typealias SContact = love.forte.simbot.definition.Contact

public typealias MGroup = net.mamoe.mirai.contact.Group
public typealias SGroup = love.forte.simbot.definition.Group

public typealias MMember = net.mamoe.mirai.contact.Member
public typealias SMember = love.forte.simbot.definition.Member

public typealias MFriend = net.mamoe.mirai.contact.Friend
public typealias SFriend = love.forte.simbot.definition.Friend



public interface MiraiContact {
    public val miraiContact: MContact
}


public interface MiraiUser : MiraiContact {
    public val miraiUser: MUser
}


public interface MiraiFriend : MiraiUser {
    public val miraiFriend: MFriend
}


public interface MiraiMember : MiraiUser {
    public val miraiMember: MMember
}


public interface MiraiGroup {
    public val miraiGroup: MGroup
}




