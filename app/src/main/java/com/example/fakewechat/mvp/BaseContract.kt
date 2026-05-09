package com.example.fakewechat.mvp

interface BasePresenter {
    fun onDestroy()
}

interface BaseView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
}