package com.example.fakewechat.ui.chatdetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.chatdetails.ChatDetailsContract
import com.example.fakewechat.mvp.chatdetails.ChatDetailsPresenter
import com.example.fakewechat.mvp.chatdetails.ChatMessageItem
import com.example.fakewechat.repository.DataRepository
import com.example.fakewechat.ui.theme.FakeWeChatTheme
import com.example.fakewechat.ui.contactdetails.ContactDetailsActivity
import com.example.fakewechat.ui.underdevelopment.UnderDevelopmentActivity

class ChatDetailsActivity : ComponentActivity(), ChatDetailsContract.View {

    companion object {
        private const val EXTRA_CHAT_ID = "chat_id"
        private const val EXTRA_IS_GROUP = "is_group"

        fun createIntent(context: Context, chatId: String, isGroup: Boolean): Intent {
            return Intent(context, ChatDetailsActivity::class.java).apply {
                putExtra(EXTRA_CHAT_ID, chatId)
                putExtra(EXTRA_IS_GROUP, isGroup)
            }
        }
    }

    private lateinit var presenter: ChatDetailsPresenter
    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)
    private var chatTitle by mutableStateOf("")
    private var messages by mutableStateOf<List<ChatMessageItem>>(emptyList())
    private var inputText by mutableStateOf("")
    private var showEmojiPanel by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val chatId = intent.getStringExtra(EXTRA_CHAT_ID) ?: ""
        val isGroup = intent.getBooleanExtra(EXTRA_IS_GROUP, false)

        val repository = DataRepository(this)
        presenter = ChatDetailsPresenter(repository)
        presenter.attachView(this)

        setContent {
            FakeWeChatTheme {
                ChatDetailsScreen(chatId, isGroup)
            }
        }

        presenter.loadMessages(chatId, isGroup)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun ChatDetailsScreen(chatId: String, isGroup: Boolean) {
        val listState = rememberLazyListState()
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = chatTitle,
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { presenter.onMenuClicked() }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                // 消息列表区域
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFEDEDED))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            // 点击消息区域时收起表情面板
                            if (showEmojiPanel) {
                                showEmojiPanel = false
                            }
                        }
                ) {
                    when {
                        isLoading -> LoadingScreen()
                        errorMessage != null -> ErrorScreen(errorMessage!!)
                        else -> MessagesList(listState)
                    }
                }

                // 底部输入区域
                ChatInputBarWithEmoji(keyboardController)
            }
        }
    }

    @Composable
    private fun MessagesList(listState: androidx.compose.foundation.lazy.LazyListState) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
        }
    }

    @Composable
    private fun MessageBubble(message: ChatMessageItem) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isFromSelf) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isFromSelf) {
                // 对方头像
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF07C160))
                        .clickable { presenter.onAvatarClicked(message.senderId) },
                    contentAlignment = Alignment.Center
                ) {
                    if (!message.senderAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = "file:///android_asset/${message.senderAvatarUrl}",
                            contentDescription = "头像",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "头像",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (message.isFromSelf) Alignment.End else Alignment.Start
            ) {
                // 发送者名字显示 (仅对非自己发送的消息显示)
                if (!message.isFromSelf && message.senderName.isNotEmpty()) {
                    Text(
                        text = message.senderName,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 消息气泡
                when (message.type) {
                    "TEXT" -> {
                        Card(
                            modifier = Modifier.widthIn(max = 250.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.isFromSelf) Color(0xFF95EC69) else Color.White
                            )
                        ) {
                            Text(
                                text = message.content,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                    "IMAGE" -> {
                        Card(
                            modifier = Modifier.size(150.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            if (!message.mediaPath.isNullOrBlank()) {
                                AsyncImage(
                                    model = "file:///android_asset/${message.mediaPath}",
                                    contentDescription = "图片消息",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "[图片]",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Card(
                            modifier = Modifier.widthIn(max = 250.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.isFromSelf) Color(0xFF95EC69) else Color.White
                            )
                        ) {
                            Text(
                                text = "[${message.type}]",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 时间显示
                if (message.formattedTime.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.formattedTime,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            if (message.isFromSelf) {
                Spacer(modifier = Modifier.width(8.dp))
                // 自己的头像
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF07C160))
                        .clickable { presenter.onAvatarClicked(message.senderId) },
                    contentAlignment = Alignment.Center
                ) {
                    if (!message.senderAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = "file:///android_asset/${message.senderAvatarUrl}",
                            contentDescription = "头像",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "头像",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ChatInputBarWithEmoji(keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?) {
        Column {
            // 主输入栏
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 语音按钮 - 功能未实现，点击跳转占位页面
                    Text(
                        text = "🎤",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable { presenter.onVoiceClicked() }
                            .padding(4.dp),
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 输入框
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        placeholder = {
                            Text(
                                text = "输入消息...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF07C160),
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect { interaction ->
                                        when (interaction) {
                                            is androidx.compose.foundation.interaction.FocusInteraction.Focus -> {
                                                // 输入框获得焦点时隐藏表情面板
                                                if (showEmojiPanel) {
                                                    showEmojiPanel = false
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 表情按钮
                    Text(
                        text = "😊",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable {
                                if (showEmojiPanel) {
                                    // 如果表情面板已显示，则隐藏
                                    showEmojiPanel = false
                                } else {
                                    // 如果表情面板未显示，则显示并隐藏键盘
                                    keyboardController?.hide()
                                    showEmojiPanel = true
                                }
                            }
                            .padding(4.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 发送按钮或加号按钮
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                presenter.onSendMessage(inputText)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "发送",
                                tint = Color(0xFF07C160)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "更多",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { presenter.onMoreClicked() }
                                .padding(4.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }

            // 表情面板
            if (showEmojiPanel) {
                EmojiPanel(
                    onEmojiSelected = { emoji ->
                        inputText += emoji
                        // 移除自动隐藏逻辑，让用户可以连续选择表情
                    }
                )
            }
        }
    }

    @Composable
    private fun EmojiPanel(onEmojiSelected: (String) -> Unit) {
        val emojiList = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
            "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰",
            "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜",
            "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏",
            "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
            "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emojiList.chunked(8)) { emojiRow ->
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        items(emojiRow) { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .clickable {
                                        onEmojiSelected(emoji)
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF07C160))
        }
    }

    @Composable
    private fun ErrorScreen(message: String) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
    }

    override fun showLoading() {
        isLoading = true
        errorMessage = null
    }

    override fun hideLoading() {
        isLoading = false
    }

    override fun showError(message: String) {
        errorMessage = message
        isLoading = false
    }

    override fun showMessages(messages: List<ChatMessageItem>) {
        this.messages = messages
    }

    override fun showChatTitle(title: String) {
        chatTitle = title
    }

    override fun showMenuOptions() {
        startActivity(UnderDevelopmentActivity.createIntent(this, "更多功能"))
    }

    override fun showVoiceInput() {
        startActivity(UnderDevelopmentActivity.createIntent(this, "语音输入"))
    }

    override fun showEmojiPanel() {
        Toast.makeText(this, "表情面板", Toast.LENGTH_SHORT).show()
    }

    override fun showMoreOptions() {
        startActivity(UnderDevelopmentActivity.createIntent(this, "更多"))
    }

    override fun scrollToBottom() {
        // 滚动在LaunchedEffect中处理
    }

    override fun navigateToContactDetails(contact: Contact) {
        val intent = Intent(this, ContactDetailsActivity::class.java).apply {
            putExtra(ContactDetailsActivity.EXTRA_CONTACT_USER_ID, contact.userId)
            putExtra(ContactDetailsActivity.EXTRA_CONTACT_USER_NAME, contact.userName)
            putExtra(ContactDetailsActivity.EXTRA_CONTACT_REMARK_NAME, contact.remarkName)
            putExtra(ContactDetailsActivity.EXTRA_CONTACT_WX_ID, contact.wxId)
            putExtra(ContactDetailsActivity.EXTRA_CONTACT_REGION, contact.region)
            putExtra(ContactDetailsActivity.EXTRA_CONTACT_AVATAR_URL, contact.avatarUrl)
            putExtra(ContactDetailsActivity.EXTRA_CONTACT_SIGNATURE, contact.signature)
            putExtra(ContactDetailsActivity.EXTRA_CONTACT_IS_FRIEND, contact.isFriend)
        }
        startActivity(intent)
    }

    override fun clearInputText() {
        inputText = ""
    }

    override fun addNewMessage(message: ChatMessageItem) {
        messages = messages + message
    }
}