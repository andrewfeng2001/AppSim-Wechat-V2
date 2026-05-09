package com.example.fakewechat.utils

import android.content.Context
import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.runBlocking

/**
 * 消息发送状态检查工具
 * 用于AI或测试代码检查消息是否发送成功
 */
class MessageChecker(context: Context) {
    private val repository = DataRepository(context)

    /**
     * 检查消息是否发送成功
     * @param content 消息内容
     * @param receiverId 接收者ID（用户ID或群ID）
     * @param timeAfterMillis 检查这个时间点之后的消息（毫秒时间戳，0表示不限制时间）
     * @return 是否发送成功
     */
    fun checkMessageSent(
        content: String,
        receiverId: String,
        timeAfterMillis: Long = 0
    ): Boolean {
        return runBlocking {
            repository.checkMessageSent(content, receiverId, timeAfterMillis / 1000)
        }
    }

    /**
     * 检查最近发送的消息
     * @param limit 返回最近的N条消息
     * @return 最近发送的消息列表
     */
    fun getRecentSentMessages(limit: Int = 10): List<MessageInfo> {
        return runBlocking {
            repository.getRecentSentMessages(limit).map { message ->
                MessageInfo(
                    id = message.id,
                    content = message.content,
                    receiverId = message.receiverId ?: message.chatRoomId ?: "",
                    timestamp = message.timestamp.toString(),
                    isGroup = message.chatRoomId != null
                )
            }
        }
    }

    /**
     * 检查是否给特定用户发送过消息
     * @param receiverId 接收者ID
     * @return 是否发送过消息
     */
    fun hasSentMessageTo(receiverId: String): Boolean {
        return runBlocking {
            val recentMessages = repository.getRecentSentMessages(100)
            recentMessages.any { message ->
                message.receiverId == receiverId || message.chatRoomId == receiverId
            }
        }
    }

    /**
     * 消息信息数据类
     */
    data class MessageInfo(
        val id: String,
        val content: String,
        val receiverId: String,
        val timestamp: String,
        val isGroup: Boolean
    )
}

/**
 * 便捷的静态检查方法
 */
object MessageCheckerHelper {
    /**
     * 快速检查消息是否发送成功
     * 使用示例：MessageCheckerHelper.wasMessageSent(context, "你好", "user_1")
     */
    fun wasMessageSent(context: Context, content: String, receiverId: String): Boolean {
        val checker = MessageChecker(context)
        return checker.checkMessageSent(content, receiverId)
    }

    /**
     * 快速检查最近5分钟内是否发送过特定消息
     */
    fun wasMessageSentRecently(context: Context, content: String, receiverId: String): Boolean {
        val checker = MessageChecker(context)
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        return checker.checkMessageSent(content, receiverId, fiveMinutesAgo)
    }
}