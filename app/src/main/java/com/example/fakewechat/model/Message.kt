package com.example.fakewechat.model

import java.time.LocalDateTime

/**
 * 微信消息数据类
 */
data class Message(
    val id: String,                    // 消息唯一ID（eg, msg_001）
    val senderId: String,              // 发送者ID（eg, user_1）
    val receiverId: String?,           // 接收者ID，私聊时使用（eg, user_2）
    val chatRoomId: String?,           // 群聊ID，群聊时使用（eg, group_1）
    val content: String,               // 消息内容（eg, 今天天气不错啊）
    val type: MessageType,             // 消息类型（eg, MessageType.TEXT）
    val timestamp: LocalDateTime,      // 发送时间（eg, 2025-09-15T10:30:00）
    val isFromSelf: Boolean,           // 是否是自己发送的（eg, false）
    val mediaPath: String? = null,     // 媒体文件路径，图片、视频、文件等（eg, images/photo_001.jpg）
)

/**
 * 消息类型枚举
 */
enum class MessageType {
    TEXT,           // 文本消息
    IMAGE,          // 图片消息
    VOICE,          // 语音消息
    VIDEO,          // 视频消息
    FILE,           // 文件消息
    EMOJI,          // 表情消息
    LOCATION,       // 位置消息
    SYSTEM          // 系统消息（如群成员变动通知等）
}