package com.example.fakewechat.ui.moments

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.BitmapFactory
import java.io.IOException
import com.example.fakewechat.model.*
import com.example.fakewechat.mvp.moments.*
import com.example.fakewechat.repository.DataRepository
import com.example.fakewechat.ui.theme.FakeWeChatTheme
import com.example.fakewechat.ui.contactdetails.ContactDetailsActivity
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class MomentsActivity : ComponentActivity(), MomentsContract.View {
    private lateinit var presenter: MomentsPresenter
    private var currentUser by mutableStateOf<Contact?>(null)
    private var moments by mutableStateOf<List<MomentsPost>>(emptyList())
    private var contacts by mutableStateOf<List<Contact>>(emptyList())

    // 图片预览状态管理
    private var isImagePreviewVisible by mutableStateOf(false)
    private var previewImages by mutableStateOf<List<String>>(emptyList())
    private var currentImageIndex by mutableStateOf(0)

    // 交互菜单状态管理
    private var showInteractionMenu by mutableStateOf(false)
    private var selectedPostId by mutableStateOf<String?>(null)

    // 评论输入状态管理
    private var showCommentInput by mutableStateOf(false)
    private var commentText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边到边显示，移除导航栏黑框
        enableEdgeToEdge()

        // 设置导航栏颜色为透明
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        presenter = MomentsPresenter(DataRepository(this))
        presenter.attachView(this)

        setContent {
            LaunchedEffect(Unit) {
                currentUser = DataRepository(this@MomentsActivity).getCurrentUser()
                contacts = DataRepository(this@MomentsActivity).getContacts()
            }

            FakeWeChatTheme {
                MomentsScreen()
            }
        }

        presenter.loadMoments()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun showMoments(moments: List<MomentsPost>) {
        runOnUiThread {
            this.moments = moments
        }
    }

    override fun showError(message: String) {
        // Simple error handling
    }

    // 本地立即更新点赞状态的方法
    private fun toggleLikeWithPersistence(postId: String) {
        // 使用presenter的持久化点赞功能
        presenter.toggleLike(postId)
    }

    override fun showLoading() {}
    override fun hideLoading() {}

    override fun navigateToContactDetails(contact: Contact) {
        val intent = Intent(this, ContactDetailsActivity::class.java).apply {
            putExtra("extra_contact_user_id", contact.userId)
            putExtra("extra_contact_user_name", contact.userName)
            putExtra("extra_contact_remark_name", contact.remarkName)
            putExtra("extra_contact_wx_id", contact.wxId)
            putExtra("extra_contact_region", contact.region)
            putExtra("extra_contact_avatar_url", contact.avatarUrl)
            putExtra("extra_contact_signature", contact.signature)
            putExtra("extra_contact_is_friend", contact.isFriend)
        }
        startActivity(intent)
    }

    private fun findUser(userId: String): Contact? {
        return if (userId == "current_user") {
            currentUser
        } else {
            contacts.find { it.userId == userId }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MomentsScreen() {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("朋友圈", color = Color.Black) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (showCommentInput) {
                                showCommentInput = false
                                selectedPostId = null
                                commentText = ""
                            } else {
                                finish()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, "返回", tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )

                // 朋友圈内容
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            // Close comment input if open
                            if (showCommentInput) {
                                showCommentInput = false
                                selectedPostId = null
                                commentText = ""
                            }
                            // Close interaction menu if open
                            if (showInteractionMenu) {
                                showInteractionMenu = false
                                selectedPostId = null
                            }
                        }
                ) {
                    item {
                        HeaderSection()
                    }

                    items(moments) { post ->
                        MomentItem(post = post, onMoreClick = {
                            selectedPostId = post.postId
                            showInteractionMenu = true
                        })
                    }
                }

                // 底部评论输入区域
                if (showCommentInput) {
                    CommentInputBar()
                }
            }

            // 图片预览组件
            ImagePreviewDialog()
        }
    }

    @Composable
    private fun AvatarImage(avatarUrl: String?, size: Int = 50) {
        val context = LocalContext.current
        var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

        LaunchedEffect(avatarUrl) {
            avatarUrl?.let { url ->
                try {
                    val inputStream = context.assets.open(url)
                    bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                } catch (e: IOException) {
                    bitmap = null
                }
            }
        }

        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray)
        ) {
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "头像",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                Text(
                    text = "😀",
                    fontSize = (size * 0.4).sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    @Composable
    private fun PostImage(imagePath: String, size: Int = 80, onClick: () -> Unit = {}) {
        val context = LocalContext.current
        var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

        LaunchedEffect(imagePath) {
            try {
                val inputStream = context.assets.open(imagePath)
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: IOException) {
                bitmap = null
            }
        }

        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF0F0F0))
                .clickable { onClick() }
        ) {
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "朋友圈图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                Text(
                    text = "🖼️",
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    @Composable
    private fun HeaderSection() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF4A90E2))
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentUser?.let { user ->
                    Text(
                        text = user.userName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    AvatarImage(user.avatarUrl, 60)
                }
            }
        }
    }

    @Composable
    private fun MomentItem(post: MomentsPost, onMoreClick: () -> Unit) {
        val author = findUser(post.authorId)
        // 获取最新的post数据，确保UI反映最新状态
        val currentPost = moments.find { it.postId == post.postId } ?: post

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 可点击的头像
                    Box(
                        modifier = Modifier.clickable {
                            presenter.onUserClicked(currentPost.authorId)
                        }
                    ) {
                        AvatarImage(author?.avatarUrl, 50)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // 可点击的用户名
                        Text(
                            text = author?.remarkName ?: author?.userName ?: "未知用户",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF576B95),
                            modifier = Modifier.clickable {
                                presenter.onUserClicked(currentPost.authorId)
                            }
                        )

                        currentPost.content?.let { content ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = content,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }

                        if (currentPost.images.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ImageGrid(images = currentPost.images)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentPost.timestamp.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = "⋯",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.clickable { onMoreClick() }
                            )
                        }

                        // 显示交互工具栏（如果当前帖子被选中）- 使用最新数据
                        if (selectedPostId == currentPost.postId && showInteractionMenu) {
                            Spacer(modifier = Modifier.height(8.dp))
                            InteractionToolbar(post = currentPost)
                        }

                        // 显示点赞和评论区域 - 使用最新数据
                        if (currentPost.likes.isNotEmpty() || currentPost.comments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            InteractionSection(post = currentPost)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ImageGrid(images: List<String>) {
        val rows = (images.size + 2) / 3

        Column {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        if (index < images.size) {
                            PostImage(
                                imagePath = images[index],
                                size = 80,
                                onClick = {
                                    previewImages = images
                                    currentImageIndex = index
                                    isImagePreviewVisible = true
                                }
                            )
                        }
                    }
                }
                if (row < rows - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    @Composable
    private fun InteractionSection(post: MomentsPost) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFFF7F7F7),
                    RoundedCornerShape(4.dp)
                )
                .padding(8.dp)
        ) {
            if (post.likes.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "❤️",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    // 可点击的点赞用户列表 - 使用ClickableText实现自动换行
                    ClickableText(
                        text = buildAnnotatedString {
                            post.likes.forEachIndexed { index, userId ->
                                val user = findUser(userId)
                                val displayName = user?.remarkName ?: user?.userName ?: "未知用户"

                                pushStringAnnotation(tag = "USER", annotation = userId)
                                append(displayName)
                                pop()

                                if (index < post.likes.size - 1) {
                                    append("，")
                                }
                            }
                        },
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xFF576B95)
                        ),
                        onClick = { offset ->
                            val annotatedString = buildAnnotatedString {
                                post.likes.forEachIndexed { index, userId ->
                                    val user = findUser(userId)
                                    val displayName = user?.remarkName ?: user?.userName ?: "未知用户"

                                    pushStringAnnotation(tag = "USER", annotation = userId)
                                    append(displayName)
                                    pop()

                                    if (index < post.likes.size - 1) {
                                        append("，")
                                    }
                                }
                            }
                            annotatedString.getStringAnnotations(tag = "USER", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    presenter.onUserClicked(annotation.item)
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (post.comments.isNotEmpty()) {
                if (post.likes.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 0.5.dp,
                        color = Color(0xFFE0E0E0)
                    )
                }

                post.comments.forEach { comment ->
                    val commentAuthor = findUser(comment.authorId)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 可点击的评论者名字
                        Text(
                            text = "${commentAuthor?.remarkName ?: commentAuthor?.userName ?: "未知用户"}：",
                            fontSize = 13.sp,
                            color = Color(0xFF576B95),
                            modifier = Modifier.clickable {
                                presenter.onUserClicked(comment.authorId)
                            }
                        )
                        Text(
                            text = comment.content,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }

    @Composable
    private fun ImagePreviewDialog() {
        if (isImagePreviewVisible && previewImages.isNotEmpty()) {
            Dialog(
                onDismissRequest = { isImagePreviewVisible = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { isImagePreviewVisible = false }
                ) {
                    // 关闭按钮和图片计数
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentImageIndex + 1} / ${previewImages.size}",
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        IconButton(
                            onClick = { isImagePreviewVisible = false }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // 图片展示
                    PreviewImage(
                        imagePath = previewImages[currentImageIndex],
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    @Composable
    private fun PreviewImage(
        imagePath: String,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

        LaunchedEffect(imagePath) {
            try {
                val inputStream = context.assets.open(imagePath)
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: IOException) {
                bitmap = null
            }
        }

        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "预览图片",
                contentScale = ContentScale.Fit,
                modifier = modifier.fillMaxSize()
            )
        }
    }

    @Composable
    private fun InteractionToolbar(post: MomentsPost) {
        // 使用固定的currentUserId确保一致性
        val currentUserId = currentUser?.userId ?: "current_user"
        val isLiked = post.likes.contains(currentUserId)

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
            shape = RoundedCornerShape(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 点赞按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            // 使用持久化的点赞功能
                            toggleLikeWithPersistence(post.postId)
                            showInteractionMenu = false
                            selectedPostId = null
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isLiked) "取消赞" else "赞",
                        tint = if (isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isLiked) "取消" else "赞",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                // 评论按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            showInteractionMenu = false
                            showCommentInput = true
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "💬",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "评论",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }

    @Composable
    private fun CommentInputBar() {
        val coroutineScope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("说点什么...") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A90E2),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            coroutineScope.launch {
                                selectedPostId?.let { postId ->
                                    addCommentWithPersistence(postId, commentText.trim())
                                }
                                keyboardController?.hide()
                                showCommentInput = false
                                selectedPostId = null
                                commentText = ""
                            }
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text("发送")
                }
            }
        }

        // 自动聚焦并显示键盘
        LaunchedEffect(showCommentInput) {
            if (showCommentInput) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }

        // 点击外部区域取消评论
        LaunchedEffect(showCommentInput) {
            if (!showCommentInput) {
                keyboardController?.hide()
                commentText = ""
                selectedPostId = null
            }
        }
    }

    private suspend fun addCommentWithPersistence(postId: String, content: String) {
        try {
            // 使用presenter的持久化评论功能
            presenter.addComment(postId, content)
        } catch (e: Exception) {
            // 处理错误
            android.util.Log.e("MomentsActivity", "添加评论失败: ${e.message}")
        }
    }
}

