package com.example.fakewechat.mvp.main

import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainPresenter(
    private val repository: DataRepository
) : MainContract.Presenter {

    private var view: MainContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun attachView(view: MainContract.View) {
        this.view = view
    }

    override fun onTabSelected(tabIndex: Int) {
        when (tabIndex) {
            0 -> view?.navigateToChat()
            1 -> view?.navigateToContacts()
            2 -> view?.navigateToDiscover()
            3 -> view?.navigateToMe()
        }
    }

    override fun loadInitialData() {
        presenterScope.launch {
            try {
                view?.showLoading()

                // 预加载应用配置数据
                withContext(Dispatchers.IO) {
                    repository.getAppConfig()
                }

                view?.hideLoading()
                view?.showMainContent()

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("数据加载失败: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        view = null
        presenterScope.coroutineContext[Job]?.cancel()
    }
}