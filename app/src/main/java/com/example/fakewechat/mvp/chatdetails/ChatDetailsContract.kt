package com.example.fakewechat.mvp.chatdetails

import com.example.fakewechat.model.JsonMessage
import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.BasePresenter
import com.example.fakewechat.mvp.BaseView

interface ChatDetailsContract {

    interface View : BaseView {
        fun showMessages(messages: List<ChatMessageItem>)
        fun showChatTitle(title: String)
        fun showMenuOptions()
        fun showVoiceInput()
        fun showEmojiPanel()
        fun showMoreOptions()
        fun scrollToBottom()
        fun navigateToContactDetails(contact: Contact)
        fun clearInputText()
        fun addNewMessage(message: ChatMessageItem)
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun loadMessages(chatId: String, isGroup: Boolean)
        fun onMenuClicked()
        fun onVoiceClicked()
        fun onEmojiClicked()
        fun onMoreClicked()
        fun onSendMessage(content: String)
        fun onAvatarClicked(userId: String)
    }
}

data class ChatMessageItem(
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String?,
    val content: String,
    val type: String,
    val timestamp: String,
    val isFromSelf: Boolean,
    val mediaPath: String?,
    val formattedTime: String
)