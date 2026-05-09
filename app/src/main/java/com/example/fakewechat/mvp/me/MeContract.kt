package com.example.fakewechat.mvp.me

import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.BasePresenter
import com.example.fakewechat.mvp.BaseView

interface MeContract {

    interface View : BaseView {
        fun showUserProfile(user: Contact)
        fun navigateToProfileEdit()
        fun navigateToService()
        fun navigateToFavorites()
        fun navigateToAlbum()
        fun navigateToCardPackage()
        fun navigateToEmoji()
        fun navigateToSettings()
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun loadUserProfile()
        fun onProfileEditClicked()
        fun onServiceClicked()
        fun onFavoritesClicked()
        fun onAlbumClicked()
        fun onCardPackageClicked()
        fun onEmojiClicked()
        fun onSettingsClicked()
        fun refreshProfile()
    }
}

data class MenuSection(
    val title: String,
    val items: List<MenuItem>
)

data class MenuItem(
    val title: String,
    val icon: String? = null,
    val badge: String? = null,
    val onClick: () -> Unit
)