package com.example.fakewechat.mvp.chat

import com.example.fakewechat.model.ChatRoom
import com.example.fakewechat.model.Contact
import com.example.fakewechat.model.Message
import com.example.fakewechat.mvp.BasePresenter
import com.example.fakewechat.mvp.BaseView

interface ChatContract {

    interface View : BaseView {
        fun showChatList(chatItems: List<ChatListItem>)
        fun navigateToChatDetail(chatId: String, isGroup: Boolean)
        fun updateChatList()
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun loadChatList()
        fun onChatItemClicked(chatId: String, isGroup: Boolean)
        fun refreshChatList()
    }
}

data class ChatListItem(
    val chatId: String,
    val title: String,
    val avatarUrl: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false
)