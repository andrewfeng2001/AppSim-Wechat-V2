package com.example.fakewechat.mvp.me

import com.example.fakewechat.model.Contact
import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MePresenter(
    private val repository: DataRepository
) : MeContract.Presenter {

    private var view: MeContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun attachView(view: MeContract.View) {
        this.view = view
    }

    override fun loadUserProfile() {
        presenterScope.launch {
            try {
                view?.showLoading()

                val currentUser = withContext(Dispatchers.IO) {
                    repository.getCurrentUser()
                }

                view?.hideLoading()
                view?.showUserProfile(currentUser)

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("用户信息加载失败: ${e.message}")
            }
        }
    }

    override fun onProfileEditClicked() {
        view?.navigateToProfileEdit()
    }

    override fun onServiceClicked() {
        view?.navigateToService()
    }

    override fun onFavoritesClicked() {
        view?.navigateToFavorites()
    }

    override fun onAlbumClicked() {
        view?.navigateToAlbum()
    }

    override fun onCardPackageClicked() {
        view?.navigateToCardPackage()
    }

    override fun onEmojiClicked() {
        view?.navigateToEmoji()
    }

    override fun onSettingsClicked() {
        view?.navigateToSettings()
    }

    override fun refreshProfile() {
        loadUserProfile()
    }

    override fun onDestroy() {
        view = null
        presenterScope.coroutineContext[Job]?.cancel()
    }
}