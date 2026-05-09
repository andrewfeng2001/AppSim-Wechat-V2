package com.example.fakewechat.mvp.moments

import com.example.fakewechat.model.MomentsPost
import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.BaseView
import com.example.fakewechat.mvp.BasePresenter

interface MomentsContract {
    interface View : BaseView {
        fun showMoments(moments: List<MomentsPost>)
        fun navigateToContactDetails(contact: Contact)
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun detachView()
        fun loadMoments()
        fun toggleLike(postId: String)
        fun onUserClicked(userId: String)
        fun addComment(postId: String, content: String)
    }
}