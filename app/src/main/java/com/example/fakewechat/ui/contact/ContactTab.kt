package com.example.fakewechat.ui.contact

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fakewechat.model.Contact
import com.example.fakewechat.mvp.contact.ContactContract
import com.example.fakewechat.mvp.contact.ContactPresenter
import com.example.fakewechat.repository.DataRepository
import com.example.fakewechat.ui.contactdetails.ContactDetailsActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactTab() {
    val context = LocalContext.current
    val repository = remember { DataRepository(context) }
    val presenter = remember { ContactPresenter(repository) }

    var friends by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var groupedFriends by remember { mutableStateOf<Map<String, List<Contact>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val view = remember {
        object : ContactContract.View {
            override fun showContactList(contacts: List<com.example.fakewechat.mvp.contact.ContactGroup>) {
                // 只显示好友，按字母分组
                val allContacts = contacts.flatMap { it.contacts }
                val friendsOnly = allContacts.filter { it.isFriend }
                friends = friendsOnly

                // 按首字母分组
                val grouped = friendsOnly.groupBy { contact ->
                    val displayName = contact.remarkName ?: contact.userName
                    getPinyin(displayName).first().uppercaseChar().toString()
                }

                // 自定义排序：A-Z在前，#在最后
                groupedFriends = grouped.toSortedMap { a, b ->
                    when {
                        a == "#" && b != "#" -> 1  // #排在其他字母后面
                        a != "#" && b == "#" -> -1 // 其他字母排在#前面
                        else -> a.compareTo(b)     // 其他情况按字母顺序
                    }
                }
            }

            override fun navigateToContactDetail(contact: Contact) {
                val intent = Intent(context, ContactDetailsActivity::class.java).apply {
                    putExtra(ContactDetailsActivity.EXTRA_CONTACT_USER_ID, contact.userId)
                    putExtra(ContactDetailsActivity.EXTRA_CONTACT_USER_NAME, contact.userName)
                    putExtra(ContactDetailsActivity.EXTRA_CONTACT_REMARK_NAME, contact.remarkName)
                    putExtra(ContactDetailsActivity.EXTRA_CONTACT_WX_ID, contact.wxId)
                    putExtra(ContactDetailsActivity.EXTRA_CONTACT_REGION, contact.region)
                    putExtra(ContactDetailsActivity.EXTRA_CONTACT_AVATAR_URL, contact.avatarUrl)
                    putExtra(ContactDetailsActivity.EXTRA_CONTACT_SIGNATURE, contact.signature)
                    putExtra(ContactDetailsActivity.EXTRA_CONTACT_IS_FRIEND, contact.isFriend)
                }
                context.startActivity(intent)
            }

            override fun showSearchResults(results: List<Contact>) {
                // 暂不处理搜索结果
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
        presenter.loadContacts()
    }

    DisposableEffect(Unit) {
        onDispose {
            presenter.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEDEDED))
        ) {
            // 功能区
            item {
                ContactFunctionSection()
            }

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
                groupedFriends.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无好友",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    // 好友列表按字母分组
                    groupedFriends.forEach { (letter, contacts) ->
                        item {
                            // 字母分组标题
                            Text(
                                text = letter,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8E8E8))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF666666)
                            )
                        }

                        items(contacts) { contact ->
                            FriendItemRow(
                                contact = contact,
                                onClick = {
                                    presenter.onContactClicked(contact)
                                }
                            )
                        }
                    }
                }
            }

            // 底部统计
            if (friends.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${friends.size}个朋友",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // 右侧字母导航
        AlphabetNavigator(
            letters = groupedFriends.keys.toList(),
            onLetterClick = { letter ->
                coroutineScope.launch {
                    // 找到对应字母的位置并滚动
                    val position = calculateScrollPosition(groupedFriends, letter)
                    if (position >= 0) {
                        listState.animateScrollToItem(position)
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

// 功能区组件
@Composable
private fun ContactFunctionSection() {
    val functionItems = listOf(
        FunctionItem("新的朋友", Icons.Default.Person, Color(0xFFFF8A00)),
        FunctionItem("群聊", Icons.Default.Person, Color(0xFF07C160)),
        FunctionItem("标签", Icons.Default.Star, Color(0xFF576B95)),
        FunctionItem("服务号", Icons.Default.Settings, Color(0xFFE74C3C))
    )

    Column {
        functionItems.forEach { item ->
            FunctionItemRow(
                item = item,
                onClick = {
                    // TODO: 处理功能点击
                }
            )
        }

        // 分隔间距
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// 功能项数据类
data class FunctionItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color
)

// 功能项行组件
@Composable
private fun FunctionItemRow(
    item: FunctionItem,
    onClick: () -> Unit
) {
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
            // 图标
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
}

// 好友项行组件
@Composable
private fun FriendItemRow(
    contact: Contact,
    onClick: () -> Unit
) {
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
            // 头像
            if (!contact.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = "file:///android_asset/${contact.avatarUrl}",
                    contentDescription = "头像",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF07C160)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "头像",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 好友名字（优先显示备注名）
            Text(
                text = contact.remarkName ?: contact.userName,
                fontSize = 17.sp,
                color = Color.Black
            )
        }
    }
}

// 字母导航组件
@Composable
private fun AlphabetNavigator(
    letters: List<String>,
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 按A-Z，最后#的顺序排列
    val allLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray().map { it.toString() } + "#"

    Column(
        modifier = modifier
            .background(Color.Transparent)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // verticalArrangement = Arrangement.spacedBy(1.dp)
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        allLetters.forEach { letter ->
            Text(
                text = letter,
                fontSize = 12.sp,
                color = Color(0xFF999999),
                modifier = Modifier
                    .clickable {
                        // 所有字母都可以点击，包括#
                        onLetterClick(letter)
                    }
                    .padding(vertical = 0.1.dp),
                fontWeight = if (letters.contains(letter)) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// 计算滚动位置的辅助函数
private fun calculateScrollPosition(groupedFriends: Map<String, List<Contact>>, targetLetter: String): Int {
    var position = 1 // 从1开始，因为0是功能区

    for ((letter, contacts) in groupedFriends) {
        if (letter == targetLetter) {
            return position
        }
        position += 1 + contacts.size // 1个标题 + N个联系人
    }

    return -1
}

// 中文拼音首字母映射表
private val pinyinMap = mapOf(
    // 常见姓氏和名字
    "王" to "W", "李" to "L", "张" to "Z", "刘" to "L", "陈" to "C", "杨" to "Y", "赵" to "Z", "黄" to "H",
    "周" to "Z", "吴" to "W", "徐" to "X", "孙" to "S", "胡" to "H", "朱" to "Z", "高" to "G", "林" to "L",
    "何" to "H", "郭" to "G", "马" to "M", "罗" to "L", "梁" to "L", "宋" to "S", "郑" to "Z", "谢" to "X",
    "韩" to "H", "唐" to "T", "冯" to "F", "于" to "Y", "董" to "D", "萧" to "X", "程" to "C", "曹" to "C",
    "袁" to "Y", "邓" to "D", "许" to "X", "傅" to "F", "沈" to "S", "曾" to "Z", "彭" to "P", "吕" to "L",
    "苏" to "S", "卢" to "L", "蒋" to "J", "蔡" to "C", "贾" to "J", "丁" to "D", "魏" to "W", "薛" to "X",
    "叶" to "Y", "阎" to "Y", "余" to "Y", "潘" to "P", "杜" to "D", "戴" to "D", "夏" to "X", "钟" to "Z",
    "汪" to "W", "田" to "T", "任" to "R", "姜" to "J", "范" to "F", "方" to "F", "石" to "S", "姚" to "Y",
    "谭" to "T", "廖" to "L", "邹" to "Z", "熊" to "X", "金" to "J", "陆" to "L", "郝" to "H", "孔" to "K",
    "崔" to "C", "常" to "C", "康" to "K", "牛" to "N", "龚" to "G", "米" to "M", "温" to "W", "代" to "D",
    "侯" to "H", "顾" to "G", "孟" to "M", "邵" to "S", "龙" to "L", "万" to "W", "段" to "D", "章" to "Z",
    "钱" to "Q", "汤" to "T", "尹" to "Y", "黎" to "L", "易" to "Y", "武" to "W", "乔" to "Q", "贺" to "H",
    "赖" to "L", "文" to "W", "史" to "S", "陶" to "T",

    // 常见字符
    "数" to "S", "学" to "X", "老" to "L", "师" to "S", "大" to "D", "室" to "S", "友" to "Y",
    "同" to "T", "事" to "S", "家" to "J", "庭" to "T", "医" to "Y", "生" to "S", "健" to "J",
    "身" to "S", "教" to "J", "练" to "L", "设" to "S", "计" to "J", "朋" to "P",

    "晨" to "C", "曦" to "X", "梦" to "M", "琪" to "Q", "杰" to "J", "乐" to "L", "思" to "S",
    "雨" to "Y", "云" to "Y", "飞" to "F", "晓" to "X", "萌" to "M", "悦" to "Y", "彤" to "T",
    "浩" to "H", "然" to "R", "诗" to "S", "涵" to "H", "志" to "Z", "豪" to "H", "美" to "M",
    "琳" to "L", "俊" to "J", "雨" to "Y", "轩" to "X", "慧" to "H", "敏" to "M", "晨" to "C",
    "阳" to "Y", "桐" to "T", "远" to "Y", "恒" to "H", "安" to "A", "宇" to "Y", "轩" to "X",
    "泽" to "Z", "涛" to "T", "鑫" to "X", "磊" to "L", "强" to "Q", "军" to "J", "平" to "P",
    "伟" to "W", "刚" to "G", "勇" to "Y", "毅" to "Y", "俊" to "J", "峰" to "F", "超" to "C",
    "越" to "Y", "斌" to "B", "文" to "W", "辉" to "H", "力" to "L", "明" to "M", "永" to "Y",
    "健" to "J", "世" to "S", "广" to "G", "志" to "Z", "义" to "Y", "兴" to "X", "良" to "L",
    "海" to "H", "山" to "S", "仁" to "R", "波" to "B", "宁" to "N", "贵" to "G", "福" to "F",

    // 添加更多常用字符
    "小" to "X", "新" to "X", "中" to "Z", "国" to "G", "华" to "H", "东" to "D", "南" to "N",
    "西" to "X", "北" to "B", "春" to "C", "夏" to "X", "秋" to "Q", "冬" to "D", "青" to "Q",
    "红" to "H", "绿" to "L", "蓝" to "L", "白" to "B", "黑" to "H", "金" to "J", "银" to "Y",
    "天" to "T", "地" to "D", "人" to "R", "和" to "H", "爱" to "A", "情" to "Q", "心" to "X",
    "愿" to "Y", "望" to "W", "信" to "X", "任" to "R", "真" to "Z", "善" to "S", "美" to "M"
)

// 改进的拼音获取函数
private fun getPinyin(text: String): String {
    if (text.isEmpty()) return "#"

    val firstChar = text.first().toString()

    return when {
        // 检查拼音映射表
        pinyinMap.containsKey(firstChar) -> pinyinMap[firstChar]!!
        // 英文字母直接返回大写
        firstChar.first().isLetter() -> firstChar.uppercase()
        // 数字和其他字符返回#
        else -> "#"
    }
}