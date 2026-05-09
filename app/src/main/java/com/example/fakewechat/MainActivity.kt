package com.example.fakewechat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fakewechat.mvp.main.MainContract
import com.example.fakewechat.mvp.main.MainPresenter
import com.example.fakewechat.repository.DataRepository
import com.example.fakewechat.ui.theme.FakeWeChatTheme
import com.example.fakewechat.ui.chat.ChatTab
import com.example.fakewechat.ui.contact.ContactTab
import com.example.fakewechat.ui.discover.DiscoverTab
import com.example.fakewechat.ui.me.MeTab
import com.example.fakewechat.ui.search.SearchActivity

class MainActivity : ComponentActivity(), MainContract.View {

    private lateinit var presenter: MainPresenter
    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)
    private var showMainContent by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = DataRepository(this)
        presenter = MainPresenter(repository)
        presenter.attachView(this)
        presenter.loadInitialData()

        setContent {
            FakeWeChatTheme {
                MainScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // 只在非"我"页面显示TopBar
                if (selectedTabIndex != 3) {
                    TopAppBar(
                        title = {
                            Text(
                                text = getTabTitle(selectedTabIndex),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            if (selectedTabIndex == 0 || selectedTabIndex == 1 || selectedTabIndex == 2) {
                                // 只在微信/通讯录/发现3个页面显示搜索图标
                                IconButton(onClick = {
                                    startActivity(Intent(this@MainActivity, SearchActivity::class.java))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "搜索",
                                        tint = Color.Black
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        selectedTabIndex = index
                        presenter.onTabSelected(index)
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    isLoading -> LoadingScreen()
                    errorMessage != null -> ErrorScreen(errorMessage!!)
                    showMainContent -> MainContent(selectedTabIndex)
                }
            }
        }
    }

    private fun getTabTitle(selectedTabIndex: Int): String {
        return when (selectedTabIndex) {
            0 -> "微信"
            1 -> "通讯录"
            2 -> "发现"
            3 -> "我"
            else -> ""
        }
    }

    @Composable
    private fun BottomNavigationBar(
        selectedTabIndex: Int,
        onTabSelected: (Int) -> Unit
    ) {
        val tabs = listOf(
            BottomNavItem("微信", Icons.Default.Home),
            BottomNavItem("通讯录", Icons.Default.Person),
            BottomNavItem("发现", Icons.Default.Search),
            BottomNavItem("我", Icons.Default.AccountCircle)
        )

        NavigationBar(
            containerColor = Color.White,
            modifier = Modifier.height(80.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (selectedTabIndex == index) Color(0xFF07C160) else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            text = tab.title,
                            color = if (selectedTabIndex == index) Color(0xFF07C160) else Color.Gray
                        )
                    },
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }

    @Composable
    private fun MainContent(selectedTabIndex: Int) {
        when (selectedTabIndex) {
            0 -> ChatTabContent()
            1 -> ContactTabContent()
            2 -> DiscoverTabContent()
            3 -> MeTabContent()
        }
    }

    @Composable
    private fun ChatTabContent() {
        ChatTab()
    }

    @Composable
    private fun ContactTabContent() {
        ContactTab()
    }

    @Composable
    private fun DiscoverTabContent() {
        DiscoverTab()
    }

    @Composable
    private fun MeTabContent() {
        MeTab()
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF07C160))
        }
    }

    @Composable
    private fun ErrorScreen(message: String) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
    }

    data class BottomNavItem(
        val title: String,
        val icon: ImageVector
    )

    override fun showLoading() {
        isLoading = true
    }

    override fun hideLoading() {
        isLoading = false
    }

    override fun showError(message: String) {
        errorMessage = message
    }

    override fun showMainContent() {
        showMainContent = true
    }

    override fun navigateToChat() {
        // Tab navigation handled in Compose state
    }

    override fun navigateToContacts() {
        // Tab navigation handled in Compose state
    }

    override fun navigateToDiscover() {
        // Tab navigation handled in Compose state
    }

    override fun navigateToMe() {
        // Tab navigation handled in Compose state
    }
}