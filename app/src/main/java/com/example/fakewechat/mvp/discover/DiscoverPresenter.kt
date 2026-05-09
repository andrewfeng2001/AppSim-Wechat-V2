package com.example.fakewechat.mvp.discover

import com.example.fakewechat.model.Contact
import com.example.fakewechat.model.MomentsPost
import com.example.fakewechat.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DiscoverPresenter(
    private val repository: DataRepository
) : DiscoverContract.Presenter {

    private var view: DiscoverContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())
    private val currentUserId = "current_user"

    override fun attachView(view: DiscoverContract.View) {
        this.view = view
    }

    override fun loadMomentsPosts() {
        presenterScope.launch {
            try {
                view?.showLoading()

                val postsDisplay = withContext(Dispatchers.IO) {
                    buildMomentsPostsDisplay()
                }

                view?.hideLoading()
                view?.showMomentsPosts(postsDisplay)

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError("朋友圈加载失败: ${e.message}")
            }
        }
    }

    private suspend fun buildMomentsPostsDisplay(): List<MomentsPostDisplay> {
        val posts = repository.getMomentsPosts()
        val appConfig = repository.getAppConfig()
        val allContacts = appConfig.contacts

        return posts.map { post ->
            val author = allContacts.find { it.userId == post.authorId }
            val isLikedByCurrentUser = post.likes.contains(currentUserId)

            val likeUsers = post.likes.mapNotNull { userId ->
                allContacts.find { it.userId == userId }
            }

            val commentDisplays = post.comments.map { comment ->
                val commentAuthor = allContacts.find { it.userId == comment.authorId }
                val replyToAuthor = if (comment.replyToId != null) {
                    val replyToComment = post.comments.find { it.commentId == comment.replyToId }
                    replyToComment?.let { replyComment ->
                        allContacts.find { it.userId == replyComment.authorId }
                    }
                } else null

                CommentDisplay(
                    commentId = comment.commentId,
                    author = commentAuthor,
                    content = comment.content,
                    timestamp = formatDateTime(comment.timestamp),
                    replyTo = replyToAuthor
                )
            }

            MomentsPostDisplay(
                post = post,
                author = author,
                isLikedByCurrentUser = isLikedByCurrentUser,
                likeUsers = likeUsers,
                commentDisplays = commentDisplays
            )
        }.sortedByDescending { it.post.timestamp }
    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        return when {
            dateTime.toLocalDate() == now.toLocalDate() -> {
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            dateTime.toLocalDate() == now.toLocalDate().minusDays(1) -> {
                "昨天 ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }
            else -> {
                dateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
            }
        }
    }

    override fun onPostLiked(postId: String) {
        presenterScope.launch {
            try {
                // 这里应该调用repository更新点赞状态
                // 为了演示，我们暂时在UI上更新
                view?.showPostLiked(postId, true)

            } catch (e: Exception) {
                view?.showError("点赞失败: ${e.message}")
            }
        }
    }

    override fun onPostClicked(postId: String) {
        view?.navigateToPostDetail(postId)
    }

    override fun onCommentClicked(postId: String) {
        // TODO: 打开评论界面
    }

    override fun refreshMoments() {
        loadMomentsPosts()
    }

    override fun onDestroy() {
        view = null
        presenterScope.coroutineContext[Job]?.cancel()
    }
}