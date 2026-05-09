package com.example.fakewechat.mvp.contactdetails

import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.BasePresenter
import com.example.fakewechat.mvp.BaseView

interface ContactDetailsContract {

    interface View : BaseView {
        fun showContactDetails(contact: Contact)
        fun showFriendProfile()
        fun showMoments()
        fun showSendMessage()
        fun showVideoCall()
        fun showMoreOptions()
        fun showAddToContacts()
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun loadContactDetails(contact: Contact)
        fun onFriendProfileClicked()
        fun onMomentsClicked()
        fun onSendMessageClicked()
        fun onVideoCallClicked()
        fun onMoreOptionsClicked()
        fun onAddToContactsClicked()
    }
}