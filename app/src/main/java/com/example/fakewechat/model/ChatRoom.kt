package com.example.fakewechat.model

/**
 * 微信群聊数据类
 */
data class ChatRoom(
    val chatRoomId: String,           // 群聊ID（eg, group_1）
    val name: String,                 // 群名称（eg, 家庭群）
    val memberIds: List<String>,      // 成员ID列表（eg, ["current_user", "user_1", "user_2"]）
    val avatarUrl: String? = null,    // 群头像URL（eg, avatar/group_1.jpg）
    val announcement: String? = null, // 群公告（eg, 家人闲聊，分享生活点滴）
    val isActive: Boolean = true      // 群聊是否活跃（eg, true）
)