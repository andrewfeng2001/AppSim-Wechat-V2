package com.example.fakewechat.mvp.contactdetails

import com.example.fakewechat.model.Contact
import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ContactDetailsPresenter(
    private val repository: DataRepository
) : ContactDetailsContract.Presenter {

    private var view: ContactDetailsContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())
    private var currentContact: Contact? = null

    override fun attachView(view: ContactDetailsContract.View) {
        this.view = view
    }

    override fun loadContactDetails(contact: Contact) {
        currentContact = contact
        view?.showContactDetails(contact)
    }

    override fun onFriendProfileClicked() {
        view?.showFriendProfile()
    }

    override fun onMomentsClicked() {
        view?.showMoments()
    }

    override fun onSendMessageClicked() {
        view?.showSendMessage()
    }

    override fun onVideoCallClicked() {
        view?.showVideoCall()
    }

    override fun onMoreOptionsClicked() {
        view?.showMoreOptions()
    }

    override fun onAddToContactsClicked() {
        view?.showAddToContacts()
    }

    override fun onDestroy() {
        view = null
        presenterScope.coroutineContext[Job]?.cancel()
    }
}