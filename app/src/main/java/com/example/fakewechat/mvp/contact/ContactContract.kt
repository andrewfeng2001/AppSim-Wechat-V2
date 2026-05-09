package com.example.fakewechat.mvp.contact

import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.BasePresenter
import com.example.fakewechat.mvp.BaseView

interface ContactContract {

    interface View : BaseView {
        fun showContactList(contacts: List<ContactGroup>)
        fun navigateToContactDetail(contact: Contact)
        fun showSearchResults(results: List<Contact>)
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun loadContacts()
        fun onContactClicked(contact: Contact)
        fun searchContacts(query: String)
        fun refreshContacts()
    }
}

data class ContactGroup(
    val title: String,
    val contacts: List<Contact>
)