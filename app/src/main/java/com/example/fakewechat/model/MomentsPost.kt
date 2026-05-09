package com.example.fakewechat.model

import java.time.LocalDateTime

/**
 * 朋友圈动态数据类
 */
data class MomentsPost(
    val postId: String,                              // 动态唯一ID（eg, post_1）
    val authorId: String,                            // 发布者ID（eg, user_1）
    val content: String? = null,                     // 文字内容（eg, 今天天气真不错，出门散步心情很好！）
    val images: List<String> = emptyList(),          // 图片URL列表（eg, ["image_1.jpg", "image_2.jpg"]）
    val timestamp: LocalDateTime,                    // 发布时间（eg, 2025-09-15T10:30:00）
    val likes: MutableList<String> = mutableListOf(), // 点赞用户ID列表（eg, ["current_user", "user_2"]）
    val comments: MutableList<Comment> = mutableListOf(), // 评论列表（eg, [Comment对象]）
    val isVisible: Boolean = true                    // 是否可见（eg, true）
)