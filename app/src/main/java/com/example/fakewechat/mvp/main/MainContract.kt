package com.example.fakewechat.mvp.main

import com.example.fakewechat.mvp.BasePresenter
import com.example.fakewechat.mvp.BaseView

interface MainContract {

    interface View : BaseView {
        fun showMainContent()
        fun navigateToChat()
        fun navigateToContacts()
        fun navigateToDiscover()
        fun navigateToMe()
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun onTabSelected(tabIndex: Int)
        fun loadInitialData()
    }
}