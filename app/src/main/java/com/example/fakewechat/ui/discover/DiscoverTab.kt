package com.example.fakewechat.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.example.fakewechat.ui.moments.MomentsActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverTab() {
    val context = LocalContext.current

    val discoverGroups = listOf(
        // 第一组：朋友圈
        listOf(
            DiscoverItem("朋友圈", Icons.Default.Person, Color(0xFF07C160))
        ),
        // 第二组：视频和直播
        listOf(
            DiscoverItem("视频号", Icons.Default.PlayArrow, Color(0xFFFF8A00)),
            DiscoverItem("直播", Icons.Default.PlayArrow, Color(0xFFFF6B35))
        ),
        // 第三组：工具类
        listOf(
            DiscoverItem("扫一扫", Icons.Default.Add, Color(0xFF576B95)),
            DiscoverItem("听一听", Icons.Default.Phone, Color(0xFFE74C3C)),
            DiscoverItem("看一看", Icons.Default.Star, Color(0xFFFFD700)),
            DiscoverItem("搜一搜", Icons.Default.Search, Color(0xFF9B59B6))
        ),
        // 第四组：服务类
        listOf(
            DiscoverItem("附近", Icons.Default.LocationOn, Color(0xFF3498DB)),
            DiscoverItem("购物", Icons.Default.ShoppingCart, Color(0xFFE91E63)),
            DiscoverItem("游戏", Icons.Default.Home, Color(0xFF2ECC71)),
            DiscoverItem("小程序", Icons.Default.Menu, Color(0xFF9C27B0))
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDEDED))
    ) {
        discoverGroups.forEachIndexed { groupIndex, group ->
            // 添加分组间距
            if (groupIndex > 0) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 渲染分组中的项目
            items(group) { item ->
                DiscoverItemRow(
                    item = item,
                    isLast = item == group.last(),
                    onClick = {
                        if (item.title == "朋友圈") {
                            val intent = Intent(context, MomentsActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}

data class DiscoverItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
private fun DiscoverItemRow(
    item: DiscoverItem,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
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
                // 图标 - 直接显示颜色，不要背景
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 标题
                Text(
                    text = item.title,
                    fontSize = 17.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // 右箭头
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "进入",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 分隔线 - 除了组内最后一个项目
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                thickness = 0.5.dp,
                color = Color(0xFFE5E5E5)
            )
        }
    }
}