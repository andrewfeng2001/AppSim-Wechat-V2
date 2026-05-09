package com.example.fakewechat.mvp.search

import com.example.fakewechat.model.Contact
import com.example.fakewechat.model.ChatRoom
import com.example.fakewechat.mvp.BaseView
import com.example.fakewechat.mvp.BasePresenter

// 搜索结果的封装类
data class SearchResult(
    val contact: Contact? = null,
    val chatRoom: ChatRoom? = null
) {
    val isContact: Boolean get() = contact != null
    val isChatRoom: Boolean get() = chatRoom != null
}

interface SearchContract {

    interface View : BaseView {
        fun showSearchResults(results: List<SearchResult>)
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun detachView()
        fun searchContacts(query: String)
    }
}