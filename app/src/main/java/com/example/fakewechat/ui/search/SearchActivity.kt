package com.example.fakewechat.ui.search

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fakewechat.model.Contact
import com.example.fakewechat.model.ChatRoom
import com.example.fakewechat.mvp.search.SearchContract
import com.example.fakewechat.mvp.search.SearchPresenter
import com.example.fakewechat.mvp.search.SearchResult
import com.example.fakewechat.repository.DataRepository
import com.example.fakewechat.ui.theme.FakeWeChatTheme
import com.example.fakewechat.ui.contactdetails.ContactDetailsActivity
import com.example.fakewechat.ui.chatdetails.ChatDetailsActivity

class SearchActivity : ComponentActivity(), SearchContract.View {

    private lateinit var presenter: SearchPresenter
    private var searchQuery by mutableStateOf("")
    private var searchResults by mutableStateOf<List<SearchResult>>(emptyList())
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = DataRepository(this)
        presenter = SearchPresenter(repository)
        presenter.attachView(this)

        setContent {
            FakeWeChatTheme {
                SearchScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SearchScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 顶部搜索栏
            TopAppBar(
                title = {
                    SearchBar()
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 搜索结果列表
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF07C160))
                }
            } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "未找到相关结果",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "可搜索联系人或群聊",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(searchResults) { searchResult ->
                        if (searchResult.isContact) {
                            ContactSearchItem(
                                contact = searchResult.contact!!,
                                onClick = { navigateToContactDetails(searchResult.contact) }
                            )
                        } else if (searchResult.isChatRoom) {
                            ChatRoomSearchItem(
                                chatRoom = searchResult.chatRoom!!,
                                onClick = { navigateToChatDetails(searchResult.chatRoom) }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchBar() {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                presenter.searchContacts(query)
            },
            placeholder = {
                Text(
                    text = "搜索联系人或群聊",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            searchResults = emptyList()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除",
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF07C160),
                unfocusedBorderColor = Color.LightGray,
                cursorColor = Color(0xFF07C160)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun ContactSearchItem(
        contact: Contact,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            AsyncImage(
                model = "file:///android_asset/${contact.avatarUrl}",
                contentDescription = "头像",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 联系人信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 显示备注名（如果有）或用户名
                val displayName = contact.remarkName?.takeIf { it.isNotEmpty() } ?: contact.userName
                Text(
                    text = displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 如果有备注名，则显示真实用户名
                if (contact.remarkName?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "微信号: ${contact.wxId}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 好友状态标识
            if (contact.isFriend) {
                Text(
                    text = "好友",
                    fontSize = 12.sp,
                    color = Color(0xFF07C160),
                    modifier = Modifier
                        .background(
                            Color(0xFF07C160).copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }

    @Composable
    private fun ChatRoomSearchItem(
        chatRoom: ChatRoom,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 群头像
            AsyncImage(
                model = "file:///android_asset/${chatRoom.avatarUrl}",
                contentDescription = "群头像",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 群聊信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 群名称
                Text(
                    text = chatRoom.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 群公告
                if (!chatRoom.announcement.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chatRoom.announcement,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 群聊标识
            Text(
                text = "群聊",
                fontSize = 12.sp,
                color = Color(0xFF07C160),
                modifier = Modifier
                    .background(
                        Color(0xFF07C160).copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }

    private fun navigateToContactDetails(contact: Contact) {
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

    private fun navigateToChatDetails(chatRoom: ChatRoom) {
        val intent = ChatDetailsActivity.createIntent(this, chatRoom.chatRoomId, true)
        startActivity(intent)
    }

    override fun showSearchResults(results: List<SearchResult>) {
        searchResults = results
    }

    override fun showLoading() {
        isLoading = true
    }

    override fun hideLoading() {
        isLoading = false
    }

    override fun showError(message: String) {
        // 可以显示Toast或Snackbar
    }
}