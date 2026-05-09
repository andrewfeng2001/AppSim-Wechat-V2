package com.example.fakewechat.repository

import com.example.fakewechat.model.*
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataRepository(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, JsonDeserializer<LocalDateTime> { json, _, _ ->
            LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        })
        .setPrettyPrinting() // 添加格式化输出
        .create()

    private var appConfig: AppConfig? = null

    suspend fun getAppConfig(): AppConfig = withContext(Dispatchers.IO) {
        if (appConfig == null) {
            loadAppConfig()
        }
        appConfig!!
    }

    private suspend fun loadAppConfig(): Unit = withContext(Dispatchers.IO) {
        try {
            val contacts = loadContacts()
            val chatRooms = loadChatRooms()
            val momentsData = loadMomentsData()
            val currentUserInfo = loadCurrentUserInfo()

            appConfig = AppConfig(
                currentUserId = currentUserInfo.currentUserId,
                contacts = contacts,
                chatRooms = chatRooms,
                momentsData = momentsData,
                messages = emptyList()
            )
        } catch (e: Exception) {
            // 如果加载失败，创建默认配置
            appConfig = AppConfig(
                currentUserId = "current_user",
                contacts = emptyList(),
                chatRooms = emptyList(),
                momentsData = MomentsData(emptyList()),
                messages = emptyList()
            )
            throw e
        }
    }

    private fun loadContacts(): List<Contact> {
        val jsonString = loadJsonFromAssets("contacts.json")
        val type = object : TypeToken<Map<String, List<Contact>>>() {}.type
        val contactsMap: Map<String, List<Contact>> = gson.fromJson(jsonString, type)

        val allContacts = mutableListOf<Contact>()
        contactsMap["friends"]?.let { allContacts.addAll(it) }
        contactsMap["nonFriends"]?.let { allContacts.addAll(it) }

        return allContacts
    }

    private fun loadChatRooms(): List<ChatRoom> {
        val jsonString = loadJsonFromAssets("chatrooms.json")
        val type = object : TypeToken<Map<String, List<ChatRoom>>>() {}.type
        val chatRoomsMap: Map<String, List<ChatRoom>> = gson.fromJson(jsonString, type)

        return chatRoomsMap["chatRooms"] ?: emptyList()
    }

    private fun loadMomentsData(): MomentsData {
        val jsonString = loadJsonFromAssets("moments.json")
        val type = object : TypeToken<Map<String, List<MomentsPost>>>() {}.type
        val momentsMap: Map<String, List<MomentsPost>> = gson.fromJson(jsonString, type)

        val posts = momentsMap["momentsPosts"] ?: emptyList()
        return MomentsData(posts)
    }

    private fun loadCurrentUserInfo(): CurrentUserInfo {
        val jsonString = loadJsonFromAssets("app_config.json")
        val type = object : TypeToken<AppConfigWrapper>() {}.type
        val configWrapper: AppConfigWrapper = gson.fromJson(jsonString, type)

        return configWrapper.appConfig
    }

    private fun loadJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open("data/$fileName").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ""
        }
    }

    data class AppConfigWrapper(
        val appConfig: CurrentUserInfo
    )

    data class CurrentUserInfo(
        val currentUserId: String,
        val currentUser: Contact? = null
    )

    suspend fun getFriends(): List<Contact> = withContext(Dispatchers.IO) {
        getAppConfig().contacts.filter { it.isFriend }
    }

    suspend fun getNonFriends(): List<Contact> = withContext(Dispatchers.IO) {
        getAppConfig().contacts.filter { !it.isFriend }
    }

    suspend fun getChatRooms(): List<ChatRoom> = withContext(Dispatchers.IO) {
        getAppConfig().chatRooms
    }

    suspend fun getMomentsPosts(): List<MomentsPost> = withContext(Dispatchers.IO) {
        getAppConfig().momentsData.posts
    }

    suspend fun getCurrentUser(): Contact = withContext(Dispatchers.IO) {
        val currentUserInfo = loadCurrentUserInfo()
        currentUserInfo.currentUser ?: Contact(
            userId = currentUserInfo.currentUserId,
            userName = "我",
            avatarUrl = "avatar/me.jpg",
            signature = "这就是我的个性签名",
            region = "北京 海淀",
            wxId = "my_wechat_id"
        )
    }

    suspend fun getMessages(): MessagesData = withContext(Dispatchers.IO) {
        val jsonString = loadJsonFromAssets("messages.json")
        val type = object : TypeToken<MessagesData>() {}.type
        gson.fromJson(jsonString, type)
    }

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        getAppConfig().contacts
    }

    suspend fun getAllContacts(): List<Contact> = withContext(Dispatchers.IO) {
        getAppConfig().contacts // 这已经包含了好友和非好友
    }

    // === 消息持久化功能 ===

    /**
     * 保存消息到JSON"数据库" - /data/data/com.example.fakewechat/files/messages.json
     */
    suspend fun saveMessage(message: Message, isGroup: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            // 1. 读取现有消息数据（优先从内部存储读取）
            val messagesData = getMessagesFromStorage()

            // 2. 将Message转换为JsonMessage
            val jsonMessage = JsonMessage(
                id = message.id,
                senderId = message.senderId,
                receiverId = message.receiverId,
                chatRoomId = message.chatRoomId,
                content = message.content,
                type = message.type.name, // MessageType enum -> String
                timestamp = message.timestamp.toString(), // LocalDateTime -> String
                isFromSelf = message.isFromSelf,
                mediaPath = message.mediaPath
            )

            // 3. 创建新的消息数据副本，添加新消息
            val updatedMessagesData = if (isGroup) {
                // 群聊消息
                val groupMessages = messagesData.groupChatMessages.toMutableMap()
                val chatId = message.chatRoomId!!
                val existingMessages = groupMessages[chatId]?.toMutableList() ?: mutableListOf()
                existingMessages.add(jsonMessage)
                groupMessages[chatId] = existingMessages

                MessagesData(
                    privateChatMessages = messagesData.privateChatMessages,
                    groupChatMessages = groupMessages
                )
            } else {
                // 私聊消息
                val privateMessages = messagesData.privateChatMessages.toMutableMap()
                val chatId = message.receiverId!!
                val existingMessages = privateMessages[chatId]?.toMutableList() ?: mutableListOf()
                existingMessages.add(jsonMessage)
                privateMessages[chatId] = existingMessages

                MessagesData(
                    privateChatMessages = privateMessages,
                    groupChatMessages = messagesData.groupChatMessages
                )
            }

            // 4. 写入到内部存储的JSON文件
            saveMessagesToFile(updatedMessagesData)

        } catch (e: Exception) {
            throw Exception("保存消息失败: ${e.message}")
        }
    }

    /**
     * 检查消息是否发送成功
     */
    suspend fun checkMessageSent(
        content: String,
        receiverId: String,
        timeAfter: Long = 0
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val messagesData = getMessagesFromStorage()

            // 检查私聊消息
            val privateMessages = messagesData.privateChatMessages[receiverId] ?: emptyList()
            val foundInPrivate = privateMessages.any { message ->
                message.content == content &&
                message.isFromSelf &&
                message.senderId == "current_user" &&
                (timeAfter == 0L || parseTimestamp(message.timestamp) > timeAfter)
            }

            // 检查群聊消息（如果receiverId是群ID）
            val groupMessages = messagesData.groupChatMessages[receiverId] ?: emptyList()
            val foundInGroup = groupMessages.any { message ->
                message.content == content &&
                message.isFromSelf &&
                message.senderId == "current_user" &&
                (timeAfter == 0L || parseTimestamp(message.timestamp) > timeAfter)
            }

            foundInPrivate || foundInGroup
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取最近发送的消息
     */
    suspend fun getRecentSentMessages(limit: Int = 10): List<Message> = withContext(Dispatchers.IO) {
        try {
            val messagesData = getMessagesFromStorage()
            val allMyJsonMessages = mutableListOf<JsonMessage>()

            // 收集所有自己发送的JSON消息
            messagesData.privateChatMessages.values.forEach { messages ->
                allMyJsonMessages.addAll(messages.filter { it.isFromSelf })
            }
            messagesData.groupChatMessages.values.forEach { messages ->
                allMyJsonMessages.addAll(messages.filter { it.isFromSelf })
            }

            // 转换为Message对象，按时间排序，返回最近的消息
            allMyJsonMessages
                .map { jsonMessage -> convertJsonMessageToMessage(jsonMessage) }
                .sortedByDescending { parseTimestamp(it.timestamp.toString()) }
                .take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 将JsonMessage转换为Message
     */
    private fun convertJsonMessageToMessage(jsonMessage: JsonMessage): Message {
        return Message(
            id = jsonMessage.id,
            senderId = jsonMessage.senderId,
            receiverId = jsonMessage.receiverId,
            chatRoomId = jsonMessage.chatRoomId,
            content = jsonMessage.content,
            type = try {
                MessageType.valueOf(jsonMessage.type)
            } catch (e: Exception) {
                MessageType.TEXT // 默认为文本类型
            },
            timestamp = try {
                LocalDateTime.parse(jsonMessage.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                LocalDateTime.now() // 默认为当前时间
            },
            isFromSelf = jsonMessage.isFromSelf,
            mediaPath = jsonMessage.mediaPath
        )
    }

    /**
     * 从内部存储读取消息数据，如果不存在则从assets初始化
     */
    suspend fun getMessagesFromStorage(): MessagesData = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(context.filesDir, "messages.json")
            if (file.exists()) {
                // 从内部存储读取
                val jsonString = file.readText()
                val type = object : TypeToken<MessagesData>() {}.type
                gson.fromJson(jsonString, type)
            } else {
                // 首次使用，从assets复制到内部存储
                val assetsData = getMessages()
                saveMessagesToFile(assetsData)
                assetsData
            }
        } catch (e: Exception) {
            // 出错时回退到assets
            getMessages()
        }
    }

    /**
     * 保存消息数据到内部存储文件
     */
    private fun saveMessagesToFile(messagesData: MessagesData) {
        try {
            val jsonString = gson.toJson(messagesData)
            val file = java.io.File(context.filesDir, "messages.json")
            file.writeText(jsonString)
        } catch (e: Exception) {
            throw Exception("写入文件失败: ${e.message}")
        }
    }

    /**
     * 解析时间戳为Long类型
     */
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            val dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            dateTime.toEpochSecond(java.time.ZoneOffset.UTC)
        } catch (e: Exception) {
            0L
        }
    }

    // ========== 朋友圈数据持久化相关方法 ==========

    /**
     * 从内部存储读取朋友圈数据，如果不存在则从assets初始化
     */
    suspend fun getMomentsFromStorage(): MomentsData = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(context.filesDir, "moments.json")
            if (file.exists()) {
                // 从内部存储读取
                val jsonString = file.readText()
                val type = object : TypeToken<Map<String, List<MomentsPost>>>() {}.type
                val momentsMap: Map<String, List<MomentsPost>> = gson.fromJson(jsonString, type)
                val posts = momentsMap["momentsPosts"] ?: emptyList()
                MomentsData(posts)
            } else {
                // 首次使用，从assets复制到内部存储
                val assetsData = loadMomentsData()
                saveMomentsToFile(assetsData)
                assetsData
            }
        } catch (e: Exception) {
            // 出错时回退到assets
            loadMomentsData()
        }
    }

    /**
     * 保存朋友圈数据到内部存储文件
     */
    private fun saveMomentsToFile(momentsData: MomentsData) {
        try {
            val momentsMap = mapOf("momentsPosts" to momentsData.posts)
            val jsonString = gson.toJson(momentsMap)
            val file = java.io.File(context.filesDir, "moments.json")
            file.writeText(jsonString)
        } catch (e: Exception) {
            throw Exception("写入朋友圈文件失败: ${e.message}")
        }
    }

    /**
     * 更新朋友圈动态的点赞状态
     */
    suspend fun toggleMomentsLike(postId: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            // 读取现有朋友圈数据
            val momentsData = getMomentsFromStorage()

            // 创建可变的动态列表
            val updatedPosts = momentsData.posts.map { post ->
                if (post.postId == postId) {
                    // 创建可变的点赞列表
                    val updatedLikes = post.likes.toMutableList()
                    if (updatedLikes.contains(userId)) {
                        updatedLikes.remove(userId)  // 取消点赞
                    } else {
                        updatedLikes.add(userId)     // 添加点赞
                    }
                    // 创建新的动态对象，更新点赞列表
                    post.copy(likes = updatedLikes)
                } else {
                    post
                }
            }

            // 保存更新后的数据
            val updatedMomentsData = MomentsData(updatedPosts)
            saveMomentsToFile(updatedMomentsData)

            // 更新内存中的appConfig
            updateAppConfigMomentsData(updatedMomentsData)

        } catch (e: Exception) {
            throw Exception("保存点赞状态失败: ${e.message}")
        }
    }

    /**
     * 添加朋友圈评论
     */
    suspend fun addMomentsComment(postId: String, comment: Comment) = withContext(Dispatchers.IO) {
        try {
            // 读取现有朋友圈数据
            val momentsData = getMomentsFromStorage()

            // 创建更新后的动态列表
            val updatedPosts = momentsData.posts.map { post ->
                if (post.postId == postId) {
                    // 创建可变的评论列表
                    val updatedComments = post.comments.toMutableList()
                    updatedComments.add(comment)
                    // 创建新的动态对象，更新评论列表
                    post.copy(comments = updatedComments)
                } else {
                    post
                }
            }

            // 保存更新后的数据
            val updatedMomentsData = MomentsData(updatedPosts)
            saveMomentsToFile(updatedMomentsData)

            // 更新内存中的appConfig
            updateAppConfigMomentsData(updatedMomentsData)

        } catch (e: Exception) {
            throw Exception("保存评论失败: ${e.message}")
        }
    }

    /**
     * 更新内存中的AppConfig朋友圈数据
     */
    private fun updateAppConfigMomentsData(momentsData: MomentsData) {
        try {
            val currentConfig = appConfig
            if (currentConfig != null) {
                val updatedConfig = currentConfig.copy(momentsData = momentsData)
                appConfig = updatedConfig
            }
        } catch (e: Exception) {
            // 更新内存失败不抛异常，只记录
            android.util.Log.w("DataRepository", "更新内存中朋友圈数据失败: ${e.message}")
        }
    }
}