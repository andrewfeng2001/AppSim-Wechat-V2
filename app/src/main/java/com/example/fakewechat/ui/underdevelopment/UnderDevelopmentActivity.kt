package com.example.fakewechat.ui.underdevelopment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fakewechat.ui.theme.FakeWeChatTheme

/**
 * 通用「开发中」占位页面。
 *
 * 所有尚未实现的功能入口统一跳转到此页面，点击左上角返回即回到来源页面。
 */
class UnderDevelopmentActivity : ComponentActivity() {

    companion object {
        /** 可选的标题文案，用于在顶部栏显示来源功能名称 */
        const val EXTRA_TITLE = "extra_title"

        /**
         * 创建跳转到占位页的 Intent。
         *
         * @param title 顶部栏标题，传空则显示默认标题
         */
        fun createIntent(context: Context, title: String? = null): Intent {
            return Intent(context, UnderDevelopmentActivity::class.java).apply {
                if (!title.isNullOrBlank()) {
                    putExtra(EXTRA_TITLE, title)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val title = intent.getStringExtra(EXTRA_TITLE)

        setContent {
            FakeWeChatTheme {
                UnderDevelopmentScreen(
                    title = title,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnderDevelopmentScreen(
    title: String?,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 顶部栏，与项目内其他页面保持一致的白底黑字样式
        TopAppBar(
            title = {
                Text(
                    text = title?.takeIf { it.isNotBlank() } ?: "开发中",
                    color = Color.Black,
                    fontSize = 17.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
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

        // 提示文案与返回按钮
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEDEDED)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "开发中",
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "该功能暂未开放",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF07C160)
                    )
                ) {
                    Text(
                        text = "返回",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
