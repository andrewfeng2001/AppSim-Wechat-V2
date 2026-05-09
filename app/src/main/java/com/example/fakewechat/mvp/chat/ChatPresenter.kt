package com.example.fakewechat.mvp.chat

import com.example.fakewechat.model.Contact
import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern

class ChatPresenter(
    private val repository: DataRepository
) : ChatContract.Presenter {

    private var view: ChatContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun attachView(view: ChatContract.View) {
        this.view = view
    }

    override fun loadChatList() {
        presenterScope.launch {
            try {
                view?.showLoading()

                val chatItems = withContext(Dispatchers.IO) {
                    buildChatList()
                }

                view?.hideLoading()
                view?.showChatList(chatItems)

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("聊天列表加载失败: ${e.message}")
            }
        }
    }

    private suspend fun buildChatList(): List<ChatListItem> {
        val chatItems = mutableListOf<ChatListItem>()
        val messages = repository.getMessages()
        val contacts = repository.getContacts() // 预加载联系人列表

        // 获取好友私聊列表
        val friends = repository.getFriends()
        friends.forEach { friend ->
            val userMessages = messages.privateChatMessages[friend.userId] ?: emptyList()
            val lastMessage = userMessages.lastOrNull()

            chatItems.add(
                ChatListItem(
                    chatId = friend.userId,
                    title = friend.remarkName ?: friend.userName,
                    avatarUrl = friend.avatarUrl,
                    lastMessage = formatLastMessage(lastMessage, false, contacts),
                    lastMessageTime = formatTime(lastMessage?.timestamp),
                    unreadCount = 0,
                    isGroup = false
                )
            )
        }

        // 获取群聊列表
        val chatRooms = repository.getChatRooms()
        chatRooms.forEach { chatRoom ->
            val groupMessages = messages.groupChatMessages[chatRoom.chatRoomId] ?: emptyList()
            val lastMessage = groupMessages.lastOrNull()

            chatItems.add(
                ChatListItem(
                    chatId = chatRoom.chatRoomId,
                    title = chatRoom.name,
                    avatarUrl = chatRoom.avatarUrl,
                    lastMessage = formatLastMessage(lastMessage, true, contacts),
                    lastMessageTime = formatTime(lastMessage?.timestamp),
                    unreadCount = 0,
                    isGroup = true
                )
            )
        }

        // 按时间排序，最新的在前面
        return chatItems.sortedByDescending { parseTimestamp(it.lastMessageTime) }
    }

    private fun formatTime(timestamp: String?): String {
        if (timestamp == null) return ""

        try {
            val messageTime = LocalDateTime.parse(timestamp)
            val today = LocalDate.now()
            val messageDate = messageTime.toLocalDate()

            return if (messageDate == today) {
                // 今天显示 HH:mm 格式
                messageTime.format(ofPattern("HH:mm"))
            } else {
                // 其他日期显示 M月d日 格式
                messageTime.format(ofPattern("M月d日"))
            }
        } catch (e: Exception) {
            return ""
        }
    }

    private fun formatLastMessage(message: com.example.fakewechat.model.JsonMessage?, isGroup: Boolean, contacts: List<Contact>): String {
        if (message == null) return ""

        return when (message.type) {
            "IMAGE" -> "[图片]"
            "TEXT" -> {
                if (isGroup) {
                    if (message.isFromSelf) {
                        // 群聊中，如果是自己发的消息，直接显示内容
                        message.content
                    } else {
                        // 群聊中，如果是别人发的消息，显示"发送者:内容"
                        val senderName = getSenderName(message.senderId, contacts)
                        "$senderName: ${message.content}"
                    }
                } else {
                    // 私聊直接显示内容
                    message.content
                }
            }
            else -> message.content
        }
    }

    private fun getSenderName(senderId: String, contacts: List<Contact>): String {
        return when (senderId) {
            "current_user" -> "我"
            else -> {
                // 从联系人列表中查找发送者姓名
                val sender = contacts.find { it.userId == senderId }
                sender?.remarkName ?: sender?.userName ?: "未知用户"
            }
        }
    }

    private fun parseTimestamp(timeString: String?): LocalDateTime {
        if (timeString.isNullOrEmpty()) return LocalDateTime.MIN

        return try {
            // 如果是 HH:mm 格式，假设是今天
            if (timeString.matches(Regex("\\d{2}:\\d{2}"))) {
                val today = LocalDate.now()
                LocalDateTime.parse("${today}T$timeString:00")
            } else if (timeString.matches(Regex("\\d{1,2}月\\d{1,2}日"))) {
                // 如果是 M月d日 格式，解析为当年的日期
                val year = LocalDate.now().year
                val monthPattern = Regex("(\\d{1,2})月(\\d{1,2})日")
                val matchResult = monthPattern.find(timeString)

                if (matchResult != null) {
                    val month = matchResult.groupValues[1].padStart(2, '0')
                    val day = matchResult.groupValues[2].padStart(2, '0')
                    LocalDateTime.parse("$year-$month-${day}T00:00:00")
                } else {
                    LocalDateTime.MIN
                }
            } else {
                LocalDateTime.MIN
            }
        } catch (e: Exception) {
            LocalDateTime.MIN
        }
    }

    override fun onChatItemClicked(chatId: String, isGroup: Boolean) {
        view?.navigateToChatDetail(chatId, isGroup)
    }

    override fun refreshChatList() {
        loadChatList()
    }

    override fun onDestroy() {
        view = null
        presenterScope.coroutineContext[Job]?.cancel()
    }
}