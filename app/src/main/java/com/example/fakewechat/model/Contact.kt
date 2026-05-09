package com.example.fakewechat.model

/**
* 微信联系人数据类
*/
data class Contact(
    val userId: String,                // 用户ID（eg, user_1）
    val userName: String,              // 用户名（eg, 王晨曦）
    val remarkName: String? = null,    // 备注名（eg, 大学室友）
    val wxId: String? = null,          // 微信号（eg, wangchenxi_2024）
    val region: String? = null,        // 地区（eg, 北京 朝阳）
    val avatarUrl: String? = null,     // 头像URL（eg, avatar/friend_1.jpg）
    val signature: String? = null,     // 个性签名（eg, 生活不止眼前的苟且，还有诗和远方）
    val isFriend: Boolean = true       // 是否是好友关系（eg, true）
)
