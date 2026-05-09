package com.example.fakewechat.ui.me

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.me.MeContract
import com.example.fakewechat.mvp.me.MePresenter
import com.example.fakewechat.mvp.me.MenuItem
import com.example.fakewechat.repository.DataRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeTab() {
    val context = LocalContext.current
    val repository = remember { DataRepository(context) }
    val presenter = remember { MePresenter(repository) }

    var currentUser by remember { mutableStateOf<Contact?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = remember {
        object : MeContract.View {
            override fun showUserProfile(user: Contact) {
                currentUser = user
            }

            override fun navigateToProfileEdit() {
                // TODO: 导航到个人资料编辑页面
            }

            override fun navigateToService() {
                // TODO: 导航到服务页面
            }

            override fun navigateToFavorites() {
                // TODO: 导航到收藏页面
            }

            override fun navigateToAlbum() {
                // TODO: 导航到朋友圈页面
            }

            override fun navigateToCardPackage() {
                // TODO: 导航到卡包页面
            }

            override fun navigateToEmoji() {
                // TODO: 导航到表情页面
            }

            override fun navigateToSettings() {
                // TODO: 导航到设置页面
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
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadUserProfile()
    }

    DisposableEffect(Unit) {
        onDispose {
            presenter.onDestroy()
        }
    }

    val menuItems = remember {
        listOf(
            MenuItem("服务", onClick = { presenter.onServiceClicked() }),
            MenuItem("收藏", onClick = { presenter.onFavoritesClicked() }),
            MenuItem("朋友圈", onClick = { presenter.onAlbumClicked() }),
            MenuItem("卡包", onClick = { presenter.onCardPackageClicked() }),
            MenuItem("表情", onClick = { presenter.onEmojiClicked() }),
            MenuItem("设置", onClick = { presenter.onSettingsClicked() })
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDEDED)),
        contentPadding = PaddingValues(top = 0.dp, bottom = 72.dp)
    ) {

        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF07C160))
                    }
                }
            }
            errorMessage != null -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            currentUser != null -> {
                item {
                    // 用户信息卡片
                    UserProfileCard(
                        user = currentUser!!,
                        onProfileClick = { presenter.onProfileEditClicked() }
                    )
                }

                // 菜单项
                items(menuItems) { menuItem ->
                    MenuItemCard(item = menuItem)
                }
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    user: Contact,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像 - 使用实际头像或默认头像
            if (!user.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = "file:///android_asset/${user.avatarUrl}",
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 默认头像
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF07C160)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "默认头像",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 用户信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!user.wxId.isNullOrBlank()) {
                    Text(
                        text = "微信号：${user.wxId}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // 进入个人信息
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "进入个人信息",
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { /* TODO: 进入个人信息 */ }
            )
        }

        // 状态和转发按钮行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 96.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // + 状态按钮
            OutlinedButton(
                onClick = { /* TODO: 设置状态 */ },
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "状态",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "状态",
                    fontSize = 12.sp
                )
            }

            // 转发按钮
            OutlinedButton(
                onClick = { /* TODO: 转发 */ },
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "转发",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    // 添加间距避免与菜单重叠
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun MenuItemCard(item: MenuItem) {
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { item.onClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 菜单图标
                Icon(
                    imageVector = getMenuIcon(item.title),
                    contentDescription = item.title,
                    tint = getMenuIconColor(item.title),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 菜单标题
                Text(
                    text = item.title,
                    fontSize = 17.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // 箭头图标
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "进入",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 分隔线
        HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp),
            thickness = 0.5.dp,
            color = Color(0xFFE5E5E5)
        )
    }
}

@Composable
private fun getMenuIcon(title: String): ImageVector {
    return when (title) {
        "服务" -> Icons.Default.Build
        "收藏" -> Icons.Default.Star
        "朋友圈" -> Icons.Default.Home
        "卡包" -> Icons.Default.AccountBox
        "表情" -> Icons.Default.Face
        "设置" -> Icons.Default.Settings
        else -> Icons.Default.Menu
    }
}

@Composable
private fun getMenuIconColor(title: String): Color {
    return when (title) {
        "服务" -> Color(0xFF07C160)
        "收藏" -> Color(0xFFFF9500)
        "朋友圈" -> Color(0xFF576B95)
        "卡包" -> Color(0xFF09BB07)
        "表情" -> Color(0xFFFFBE00)
        "设置" -> Color(0xFF576B95)
        else -> Color.Gray
    }
}