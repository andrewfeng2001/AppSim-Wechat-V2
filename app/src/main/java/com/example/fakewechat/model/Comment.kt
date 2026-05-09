package com.example.fakewechat.model

import java.time.LocalDateTime

/**
 * 朋友圈评论数据类
 */
data class Comment(
    val commentId: String,           // 评论唯一ID（eg, comment_1）
    val authorId: String,            // 评论者ID（eg, user_2）
    val content: String,             // 评论内容（eg, 是啊，适合出游）
    val timestamp: LocalDateTime,    // 评论时间（eg, 2025-09-15T11:30:00）
    val replyToId: String? = null    // 回复的评论ID，用于回复评论（eg, comment_1）
)