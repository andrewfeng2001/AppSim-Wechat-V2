package com.example.fakewechat.mvp.discover

import com.example.fakewechat.model.Contact
import com.example.fakewechat.model.MomentsPost
import com.example.fakewechat.mvp.BasePresenter
import com.example.fakewechat.mvp.BaseView

interface DiscoverContract {

    interface View : BaseView {
        fun showMomentsPosts(posts: List<MomentsPostDisplay>)
        fun updatePost(postId: String, updatedPost: MomentsPostDisplay)
        fun navigateToPostDetail(postId: String)
        fun showPostLiked(postId: String, isLiked: Boolean)
    }

    interface Presenter : BasePresenter {
        fun attachView(view: View)
        fun loadMomentsPosts()
        fun onPostLiked(postId: String)
        fun onPostClicked(postId: String)
        fun onCommentClicked(postId: String)
        fun refreshMoments()
    }
}

data class MomentsPostDisplay(
    val post: MomentsPost,
    val author: Contact?,
    val isLikedByCurrentUser: Boolean,
    val likeUsers: List<Contact>,
    val commentDisplays: List<CommentDisplay>
)

data class CommentDisplay(
    val commentId: String,
    val author: Contact?,
    val content: String,
    val timestamp: String,
    val replyTo: Contact?
)