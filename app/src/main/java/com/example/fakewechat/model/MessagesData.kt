package com.example.fakewechat.model

/**
 * 消息数据结构，用于从messages.json加载数据
 */
data class MessagesData(
    val privateChatMessages: Map<String, List<JsonMessage>>,
    val groupChatMessages: Map<String, List<JsonMessage>>
)

/**
 * JSON消息模型，与messages.json结构匹配
 */
data class JsonMessage(
    val id: String,
    val senderId: String,
    val receiverId: String?,
    val chatRoomId: String?,
    val content: String,
    val type: String,
    val timestamp: String,
    val isFromSelf: Boolean,
    val mediaPath: String?
)