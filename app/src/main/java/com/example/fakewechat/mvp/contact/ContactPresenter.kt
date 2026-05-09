package com.example.fakewechat.mvp.contact

import com.example.fakewechat.model.Contact
import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactPresenter(
    private val repository: DataRepository
) : ContactContract.Presenter {

    private var view: ContactContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun attachView(view: ContactContract.View) {
        this.view = view
    }

    override fun loadContacts() {
        presenterScope.launch {
            try {
                view?.showLoading()

                val contactGroups = withContext(Dispatchers.IO) {
                    buildContactGroups()
                }

                view?.hideLoading()
                view?.showContactList(contactGroups)

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("联系人列表加载失败: ${e.message}")
            }
        }
    }

    private suspend fun buildContactGroups(): List<ContactGroup> {
        val friends = repository.getFriends()
        val nonFriends = repository.getNonFriends()

        val groups = mutableListOf<ContactGroup>()

        // 按拼音首字母分组好友
        val friendsByPinyin = friends.groupBy { contact ->
            getPinyinFirstLetter(contact.remarkName ?: contact.userName)
        }.toSortedMap()

        friendsByPinyin.forEach { (letter, contacts) ->
            groups.add(ContactGroup("好友 - $letter", contacts.sortedBy { it.remarkName ?: it.userName }))
        }

        // 如果有非好友，添加一个分组
        if (nonFriends.isNotEmpty()) {
            val nonFriendsByPinyin = nonFriends.groupBy { contact ->
                getPinyinFirstLetter(contact.userName)
            }.toSortedMap()

            nonFriendsByPinyin.forEach { (letter, contacts) ->
                groups.add(ContactGroup("其他联系人 - $letter", contacts.sortedBy { it.userName }))
            }
        }

        return groups
    }

    private fun getPinyinFirstLetter(name: String): String {
        if (name.isEmpty()) return "#"
        val firstChar = name.first().uppercaseChar()
        return if (firstChar in 'A'..'Z') {
            firstChar.toString()
        } else {
            "#"
        }
    }

    override fun onContactClicked(contact: Contact) {
        view?.navigateToContactDetail(contact)
    }

    override fun searchContacts(query: String) {
        if (query.isBlank()) {
            loadContacts()
            return
        }

        presenterScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    val allContacts = repository.getFriends() + repository.getNonFriends()
                    allContacts.filter { contact ->
                        contact.userName.contains(query, ignoreCase = true) ||
                                contact.remarkName?.contains(query, ignoreCase = true) == true ||
                                contact.wxId?.contains(query, ignoreCase = true) == true
                    }
                }

                view?.showSearchResults(results)

            } catch (e: Exception) {
                view?.showError("搜索失败: ${e.message}")
            }
        }
    }

    override fun refreshContacts() {
        loadContacts()
    }

    override fun onDestroy() {
        view = null
        presenterScope.coroutineContext[Job]?.cancel()
    }
}