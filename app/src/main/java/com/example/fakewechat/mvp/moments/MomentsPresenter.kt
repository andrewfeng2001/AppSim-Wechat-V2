package com.example.fakewechat.mvp.moments

import com.example.fakewechat.repository.DataRepository
import com.example.fakewechat.model.Comment
import kotlinx.coroutines.*
import java.time.LocalDateTime

class MomentsPresenter(private val dataRepository: DataRepository) : MomentsContract.Presenter {
    private var view: MomentsContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun attachView(view: MomentsContract.View) {
        this.view = view
    }

    override fun onDestroy() {
        detachView()
    }

    override fun detachView() {
        view = null
        job.cancel()
    }

    override fun loadMoments() {
        scope.launch {
            try {
                // 从内部存储加载朋友圈数据（支持持久化）
                val momentsData = dataRepository.getMomentsFromStorage()
                view?.showMoments(momentsData.posts)
            } catch (e: Exception) {
                view?.showError("加载朋友圈失败")
            }
        }
    }

    override fun toggleLike(postId: String) {
        scope.launch {
            try {
                val currentUser = dataRepository.getCurrentUser()

                // IO线程执行数据库操作
                withContext(Dispatchers.IO) {
                    dataRepository.toggleMomentsLike(postId, currentUser.userId)
                }

                // 主线程更新UI
                val updatedMomentsData = dataRepository.getMomentsFromStorage()
                view?.showMoments(updatedMomentsData.posts)

            } catch (e: Exception) {
                view?.showError("点赞操作失败: ${e.message}")
            }
        }
    }

    override fun onUserClicked(userId: String) {
        scope.launch {
            try {
                val contacts = dataRepository.getContacts()
                val currentUser = dataRepository.getCurrentUser()

                val contact = if (userId == "current_user") {
                    currentUser
                } else {
                    contacts.find { it.userId == userId }
                }

                contact?.let {
                    view?.navigateToContactDetails(it)
                }
            } catch (e: Exception) {
                view?.showError("跳转失败")
            }
        }
    }

    override fun addComment(postId: String, content: String) {
        scope.launch {
            try {
                val currentUser = dataRepository.getCurrentUser()

                // 创建新的评论对象
                val newComment = Comment(
                    commentId = "comment_${System.currentTimeMillis()}",
                    authorId = currentUser.userId,
                    content = content,
                    timestamp = LocalDateTime.now(),
                    replyToId = null
                )

                // IO线程执行数据库操作
                withContext(Dispatchers.IO) {
                    dataRepository.addMomentsComment(postId, newComment)
                }

                // 主线程更新UI
                val updatedMomentsData = dataRepository.getMomentsFromStorage()
                view?.showMoments(updatedMomentsData.posts)

            } catch (e: Exception) {
                view?.showError("发表评论失败: ${e.message}")
            }
        }
    }
}