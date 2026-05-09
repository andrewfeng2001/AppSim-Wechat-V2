package com.example.fakewechat.mvp.chatdetails

import com.example.fakewechat.model.Contact
import com.example.fakewechat.model.Message
import com.example.fakewechat.model.MessageType
import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatDetailsPresenter(
    private val repository: DataRepository
) : ChatDetailsContract.Presenter {

    private var view: ChatDetailsContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    // 保存当前聊天信息，用于发送消息
    private var currentChatId: String? = null
    private var currentIsGroup: Boolean = false

    override fun attachView(view: ChatDetailsContract.View) {
        this.view = view
    }

    override fun loadMessages(chatId: String, isGroup: Boolean) {
        // 保存当前聊天信息
        currentChatId = chatId
        currentIsGroup = isGroup

        presenterScope.launch {
            try {
                view?.showLoading()

                val chatTitle = withContext(Dispatchers.IO) {
                    getChatTitle(chatId, isGroup)
                }

                val messageItems = withContext(Dispatchers.IO) {
                    buildMessageItems(chatId, isGroup)
                }

                view?.hideLoading()
                view?.showChatTitle(chatTitle)
                view?.showMessages(messageItems)
                view?.scrollToBottom()

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("消息加载失败: ${e.message}")
            }
        }
    }

    private suspend fun getChatTitle(chatId: String, isGroup: Boolean): String {
        return if (isGroup) {
            val chatRooms = repository.getChatRooms()
            chatRooms.find { it.chatRoomId == chatId }?.name ?: "群聊"
        } else {
            val contacts = repository.getContacts()
            val contact = contacts.find { it.userId == chatId }
            contact?.remarkName ?: contact?.userName ?: "聊天"
        }
    }

    private suspend fun buildMessageItems(chatId: String, isGroup: Boolean): List<ChatMessageItem> {
        // 优先从内部存储读取消息（包含新发送的消息）
        val messages = try {
            repository.getMessagesFromStorage()
        } catch (e: Exception) {
            repository.getMessages() // 回退到assets
        }
        val contacts = repository.getContacts()

        val jsonMessages = if (isGroup) {
            messages.groupChatMessages[chatId] ?: emptyList()
        } else {
            messages.privateChatMessages[chatId] ?: emptyList()
        }

        return jsonMessages.map { jsonMessage ->
            val sender = contacts.find { it.userId == jsonMessage.senderId }
            val senderName = when {
                jsonMessage.isFromSelf -> "我"
                sender != null -> sender.remarkName ?: sender.userName
                else -> "未知用户"
            }

            val senderAvatarUrl = when {
                jsonMessage.isFromSelf -> {
                    val currentUser = repository.getCurrentUser()
                    currentUser.avatarUrl
                }
                sender != null -> sender.avatarUrl
                else -> null
            }

            ChatMessageItem(
                messageId = jsonMessage.id,
                senderId = jsonMessage.senderId,
                senderName = senderName,
                senderAvatarUrl = senderAvatarUrl,
                content = jsonMessage.content,
                type = jsonMessage.type,
                timestamp = jsonMessage.timestamp,
                isFromSelf = jsonMessage.isFromSelf,
                mediaPath = jsonMessage.mediaPath,
                formattedTime = formatMessageTime(jsonMessage.timestamp)
            )
        }
    }

    private fun formatMessageTime(timestamp: String): String {
        return try {
            val messageTime = LocalDateTime.parse(timestamp)
            messageTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            ""
        }
    }

    override fun onMenuClicked() {
        view?.showMenuOptions()
    }

    override fun onVoiceClicked() {
        view?.showVoiceInput()
    }

    override fun onEmojiClicked() {
        view?.showEmojiPanel()
    }

    override fun onMoreClicked() {
        view?.showMoreOptions()
    }

    override fun onSendMessage(content: String) {
        if (content.isNotBlank() && currentChatId != null) {
            presenterScope.launch {
                try {
                    val currentUser = repository.getCurrentUser()
                    val currentTime = LocalDateTime.now()
                    val messageId = "msg_${System.currentTimeMillis()}"

                    // 创建用于显示的消息项
                    val newChatMessageItem = ChatMessageItem(
                        messageId = messageId,
                        senderId = "current_user",
                        senderName = "我",
                        senderAvatarUrl = currentUser.avatarUrl,
                        content = content,
                        type = "TEXT",
                        timestamp = currentTime.toString(),
                        isFromSelf = true,
                        mediaPath = null,
                        formattedTime = formatMessageTime(currentTime.toString())
                    )

                    // 创建用于持久化的消息对象
                    val messageForStorage = Message(
                        id = messageId,
                        senderId = "current_user",
                        receiverId = if (!currentIsGroup) currentChatId else null,
                        chatRoomId = if (currentIsGroup) currentChatId else null,
                        content = content,
                        type = MessageType.TEXT,
                        timestamp = currentTime,
                        isFromSelf = true,
                        mediaPath = null
                    )

                    // 同时更新UI和保存到JSON"数据库"
                    withContext(Dispatchers.IO) {
                        repository.saveMessage(messageForStorage, currentIsGroup)
                    }

                    view?.addNewMessage(newChatMessageItem)
                    view?.clearInputText()
                    view?.scrollToBottom()

                } catch (e: Exception) {
                    view?.showError("发送消息失败: ${e.message}")
                }
            }
        }
    }

    override fun onAvatarClicked(userId: String) {
        presenterScope.launch {
            try {
                val contact = if (userId == "current_user") {
                    repository.getCurrentUser()
                } else {
                    val contacts = repository.getContacts()
                    contacts.find { it.userId == userId }
                }

                if (contact != null) {
                    view?.navigateToContactDetails(contact)
                }
            } catch (e: Exception) {
                view?.showError("无法获取联系人信息")
            }
        }
    }

    override fun onDestroy() {
        view = null
        presenterScope.coroutineContext[Job]?.cancel()
    }
}