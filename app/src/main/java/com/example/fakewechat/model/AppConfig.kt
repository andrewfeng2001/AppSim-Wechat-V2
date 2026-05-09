package com.example.fakewechat.model

/**
 * 应用配置和全局状态数据类
 */
data class AppConfig(
    val currentUserId: String,              // 当前用户ID（eg, current_user）
    val contacts: List<Contact>,            // 联系人列表（eg, [Contact对象列表]）
    val chatRooms: List<ChatRoom>,          // 群聊列表（eg, [ChatRoom对象列表]）
    val momentsData: MomentsData,           // 朋友圈数据（eg, MomentsData对象）
    val messages: List<Message> = emptyList() // 所有消息记录（eg, [Message对象列表]）
)

/**
 * 朋友圈数据容器
 */
data class MomentsData(
    val posts: List<MomentsPost>           // 所有朋友圈动态（eg, [MomentsPost对象列表]）
)