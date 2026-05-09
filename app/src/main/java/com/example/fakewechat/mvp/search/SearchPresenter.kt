package com.example.fakewechat.mvp.search

import com.example.fakewechat.model.Contact
import com.example.fakewechat.model.ChatRoom
import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPresenter(
    private val repository: DataRepository
) : SearchContract.Presenter {

    private var view: SearchContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun attachView(view: SearchContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun onDestroy() {
        detachView()
    }

    override fun searchContacts(query: String) {
        if (query.isBlank()) {
            view?.showSearchResults(emptyList())
            return
        }

        view?.showLoading()

        presenterScope.launch {
            try {
                val results = mutableListOf<SearchResult>()

                // 搜索联系人
                val allContacts = withContext(Dispatchers.IO) {
                    repository.getAllContacts()
                }

                val contactResults = allContacts.filter { contact ->
                    // 搜索用户名
                    contact.userName.contains(query, ignoreCase = false) ||
                    // 搜索备注名（如果不为空）
                    (contact.remarkName?.contains(query, ignoreCase = false) == true) ||
                    // 搜索微信号
                    (contact.wxId?.contains(query, ignoreCase = false) == true)
                }.map { contact ->
                    SearchResult(contact = contact)
                }

                // 搜索群聊
                val allChatRooms = withContext(Dispatchers.IO) {
                    repository.getChatRooms()
                }

                val chatRoomResults = allChatRooms.filter { chatRoom ->
                    // 搜索群名称
                    chatRoom.name.contains(query, ignoreCase = false) ||
                    // 搜索群公告（如果不为空）
                    (chatRoom.announcement?.contains(query, ignoreCase = false) == true)
                }.map { chatRoom ->
                    SearchResult(chatRoom = chatRoom)
                }

                // 合并搜索结果，联系人在前，群聊在后
                results.addAll(contactResults)
                results.addAll(chatRoomResults)

                view?.hideLoading()
                view?.showSearchResults(results)

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("搜索出错: ${e.message}")
            }
        }
    }
}