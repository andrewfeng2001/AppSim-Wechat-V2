package com.example.fakewechat.ui.contactdetails

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.contactdetails.ContactDetailsContract
import com.example.fakewechat.mvp.contactdetails.ContactDetailsPresenter
import com.example.fakewechat.repository.DataRepository
import com.example.fakewechat.ui.theme.FakeWeChatTheme
import com.example.fakewechat.ui.chatdetails.ChatDetailsActivity

class ContactDetailsActivity : ComponentActivity(), ContactDetailsContract.View {

    companion object {
        const val EXTRA_CONTACT_USER_ID = "extra_contact_user_id"
        const val EXTRA_CONTACT_USER_NAME = "extra_contact_user_name"
        const val EXTRA_CONTACT_REMARK_NAME = "extra_contact_remark_name"
        const val EXTRA_CONTACT_WX_ID = "extra_contact_wx_id"
        const val EXTRA_CONTACT_REGION = "extra_contact_region"
        const val EXTRA_CONTACT_AVATAR_URL = "extra_contact_avatar_url"
        const val EXTRA_CONTACT_SIGNATURE = "extra_contact_signature"
        const val EXTRA_CONTACT_IS_FRIEND = "extra_contact_is_friend"
    }

    private lateinit var presenter: ContactDetailsPresenter
    private var contact by mutableStateOf<Contact?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = DataRepository(this)
        presenter = ContactDetailsPresenter(repository)
        presenter.attachView(this)

        val intentContact = Contact(
            userId = intent.getStringExtra(EXTRA_CONTACT_USER_ID) ?: "",
            userName = intent.getStringExtra(EXTRA_CONTACT_USER_NAME) ?: "",
            remarkName = intent.getStringExtra(EXTRA_CONTACT_REMARK_NAME),
            wxId = intent.getStringExtra(EXTRA_CONTACT_WX_ID),
            region = intent.getStringExtra(EXTRA_CONTACT_REGION),
            avatarUrl = intent.getStringExtra(EXTRA_CONTACT_AVATAR_URL),
            signature = intent.getStringExtra(EXTRA_CONTACT_SIGNATURE),
            isFriend = intent.getBooleanExtra(EXTRA_CONTACT_IS_FRIEND, true)
        )

        presenter.loadContactDetails(intentContact)

        setContent {
            FakeWeChatTheme {
                ContactDetailsScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ContactDetailsScreen() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { },
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
                        IconButton(onClick = { presenter.onMoreOptionsClicked() }) {
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
            contact?.let { contactData ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFEDEDED))
                ) {
                    ContactInfoSection(contactData)
                    Spacer(modifier = Modifier.height(8.dp))
                    FunctionSection()
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionButtonsSection()
                }
            }
        }
    }

    @Composable
    private fun ContactInfoSection(contact: Contact) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (!contact.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = "file:///android_asset/${contact.avatarUrl}",
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF07C160)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "头像",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = contact.remarkName ?: contact.userName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (!contact.remarkName.isNullOrBlank() && contact.remarkName != contact.userName) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "昵称：${contact.userName}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    if (!contact.wxId.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "微信号：${contact.wxId}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun FunctionSection() {
        Column {
            FunctionItem(
                title = "朋友资料",
                subtitle = "添加朋友的备注名、电话、标签、备忘，并设置朋友权限。",
                icon = Icons.Default.Person,
                iconColor = Color(0xFF07C160),
                onClick = { presenter.onFriendProfileClicked() }
            )

            FunctionItem(
                title = "朋友圈",
                subtitle = null,
                icon = Icons.Default.Star,
                iconColor = Color(0xFF576B95),
                onClick = { presenter.onMomentsClicked() }
            )
        }
    }

    @Composable
    private fun FunctionItem(
        title: String,
        subtitle: String?,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        iconColor: Color,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal
                    )

                    subtitle?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "进入",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    @Composable
    private fun ActionButtonsSection() {
        contact?.let { contactData ->
            if (contactData.isFriend) {
                // 如果是好友，显示发消息和音视频通话按钮
                Column {
                    ActionButtonItem(
                        icon = Icons.Default.Send,
                        text = "发消息",
                        onClick = { presenter.onSendMessageClicked() }
                    )

                    ActionButtonItem(
                        icon = Icons.Default.Call,
                        text = "音视频通话",
                        onClick = { presenter.onVideoCallClicked() }
                    )
                }
            } else {
                // 如果不是好友，只显示添加到通讯录按钮
                ActionButtonItem(
                    icon = Icons.Default.Add,
                    text = "添加到通讯录",
                    onClick = { presenter.onAddToContactsClicked() }
                )
            }
        }
    }

    @Composable
    private fun ActionButtonItem(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        text: String,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Color(0xFF576B95),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    fontSize = 17.sp,
                    color = Color(0xFF576B95),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

    override fun showContactDetails(contact: Contact) {
        this.contact = contact
    }

    override fun showFriendProfile() {
        Toast.makeText(this, "朋友资料功能待实现", Toast.LENGTH_SHORT).show()
    }

    override fun showMoments() {
        Toast.makeText(this, "朋友圈功能待实现", Toast.LENGTH_SHORT).show()
    }

    override fun showSendMessage() {
        contact?.let { contactData ->
            val intent = ChatDetailsActivity.createIntent(
                context = this,
                chatId = contactData.userId,
                isGroup = false
            )
            startActivity(intent)
        }
    }

    override fun showVideoCall() {
        Toast.makeText(this, "音视频通话功能待实现", Toast.LENGTH_SHORT).show()
    }

    override fun showMoreOptions() {
        Toast.makeText(this, "更多选项功能待实现", Toast.LENGTH_SHORT).show()
    }

    override fun showAddToContacts() {
        Toast.makeText(this, "添加到通讯录功能待实现", Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {}

    override fun hideLoading() {}

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}