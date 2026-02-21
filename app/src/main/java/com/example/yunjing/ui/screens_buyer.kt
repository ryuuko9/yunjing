package com.example.yunjing.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yunjing.nav.Destinations
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.graphics.Color
import com.example.yunjing.data.AuthStore
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.yunjing.data.UserRole

/**
 * 买家主壳：自绘底部栏（避免 NavigationBarItem 自带 ripple/indication 风险）
 */
@Composable
fun BuyerMainShell(
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    val innerNav = rememberNavController()
    val tabs = remember { buyerTabs() }

    val context = LocalContext.current
    val authStore = remember(context) { AuthStore(context) }

    val username by authStore
        .accountFlow(UserRole.BUYER)
        .collectAsState(initial = "未登录")

    // 当前 route
    val navBackStackEntry by innerNav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxSize()
            .buyerSoftBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 内容区
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                BuyerTabNavHost(
                    nav = innerNav,
                    onSwitchRole = onSwitchRole,
                    onLogout = onLogout,
                    username = username
                )
            }

            // 底部栏（安全：无 ripple）
            BuyerBottomBar(
                tabs = tabs,
                currentDestination = currentDestination,
                onTabClick = { route ->
                    innerNav.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        // ✅ 关键：用 graph 的 startDestination，退出再进入也稳定
                        popUpTo(innerNav.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BuyerTabNavHost(
    nav: NavHostController,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    username: String
) {
    NavHost(
        navController = nav,
        startDestination = Destinations.BUYER_HOME
    ) {
        composable(Destinations.BUYER_HOME) {
            BuyerHomeScreen(
                onPrimaryScan = { /* TODO: 扫码/开始安装 */ },
                onQuickAi = { nav.navigate(Destinations.BUYER_AI) },
                onQuickTutorial = { nav.navigate(Destinations.BUYER_TUTORIAL) }
            )
        }
        composable(Destinations.BUYER_TUTORIAL) { BuyerTutorialScreen() }
        composable(Destinations.BUYER_AI) { BuyerAiAssistScreen() }
        composable(Destinations.BUYER_PROFILE) {
            BuyerProfileScreen(
                onSwitchRole = onSwitchRole,
                onLogout = onLogout,
                username = username
            )
        }
    }
}

/* ---------------------------
   Tab 定义 & 底部栏
---------------------------- */

// ↑ 确保有这两个 import（Icon 你可能已有）

private data class BuyerTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private fun buyerTabs(): List<BuyerTab> = listOf(
    BuyerTab(
        route = Destinations.BUYER_HOME,
        label = "首页",
        icon = Icons.Filled.Home
    ),
    BuyerTab(
        route = Destinations.BUYER_TUTORIAL,
        label = "教程",
        icon = Icons.Filled.MenuBook
    ),
    BuyerTab(
        route = Destinations.BUYER_AI,
        label = "AI",
        icon = Icons.Filled.SmartToy
    ),
    BuyerTab(
        route = Destinations.BUYER_PROFILE,
        label = "我的",
        icon = Icons.Filled.Person
    )
)

/**
 * 小 icon 包一层，后面更好统一尺寸/颜色
 */
@Composable
private fun SimpleIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null
    )
}

@Composable
private fun BuyerBottomBar(
    tabs: List<BuyerTab>,
    currentDestination: NavDestination?,
    onTabClick: (String) -> Unit
) {
    val barShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        shape = barShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { tab ->
                val selected = currentDestination
                    ?.hierarchy
                    ?.any { it.route == tab.route } == true

                BuyerBottomBarItem(
                    label = tab.label,
                    selected = selected,
                    icon = tab.icon,
                    onClick = { onTabClick(tab.route) }
                )
            }
        }
    }
}

@Composable
private fun BuyerBottomBarItem(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val targetAlpha = if (selected) 1f else 0.55f
    val alpha by animateFloatAsState(targetValue = targetAlpha, label = "tabAlpha")

    val targetScale = if (selected) 1f else 0.98f
    val scale by animateFloatAsState(targetValue = targetScale, label = "tabScale")

    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .pressClick(onClick = onClick) // ✅ 你的无 ripple 统一按压动效
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(
            LocalContentColor provides color
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = color.copy(alpha = alpha)
        )
    }
}

/* ---------------------------
   买家首页（重点优化）
---------------------------- */

private data class RecentItem(
    val title: String,
    val subtitle: String
)

@Composable
fun BuyerHomeScreen(
    onPrimaryScan: () -> Unit,
    onQuickAi: () -> Unit,
    onQuickTutorial: () -> Unit
) {
    val recent = remember {
        listOf(
            RecentItem("书架安装 · 第 3 步", "继续上次进度"),
            RecentItem("智能门锁调试", "上次查看：5 分钟前"),
            RecentItem("小家电清洁维护", "收藏的教程")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        Text(
            text = "云镜智联 · 买家",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "扫码开始安装，或用 AI 快速诊断卡点",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        // 主 CTA：扫码开始
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "开始安装",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "扫描商品二维码，自动加载教程与步骤",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))

                PrimaryPillButton(
                    text = "扫码开始",
                    onClick = onPrimaryScan
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 快捷区
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "AI 诊断",
                desc = "拍照/提问\n快速定位步骤",
                onClick = onQuickAi,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "教程库",
                desc = "按商品/品类\n查找教程",
                onClick = onQuickTutorial,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "最近",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(10.dp))

        // 最近列表（带好看的空状态）
        if (recent.isEmpty()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier
                    .fillMaxWidth()
                    .padding(18.dp)) {
                    Text(
                        text = "暂无最近记录",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "你可以先扫码开始安装，系统会自动记录进度。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    items(recent) { item ->
                        RecentRow(
                            title = item.title,
                            subtitle = item.subtitle,
                            onClick = { /* TODO: 进入该进度 */ }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    desc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(
        modifier = modifier,
        corner = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onClick)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun RecentRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .pressClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "›",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ---------------------------
   其他 Tab（先给“好看占位”，后续再接真实功能）
---------------------------- */

@Composable
fun BuyerTutorialScreen() {
    SimplePlaceholderPage(
        title = "教程库",
        subtitle = "按商品/品类查找安装与维护教程"
    )
}

@Composable
fun BuyerAiAssistScreen() {
    SimplePlaceholderPage(
        title = "AI 助手",
        subtitle = "拍照诊断卡点，或直接提问"
    )
}

@Composable
fun BuyerProfileScreen(
    username: String,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    var showSwitchConfirm by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .buyerSoftBackground()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .padding(bottom = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // 顶部标题（仿 iOS：居中、留白）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "我的账号",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(14.dp))

        // 头像+昵称区（仿图：居中、头像上方大留白）
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BuyerAvatar(
                    modifier = Modifier.size(84.dp)
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    text = username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "买家 · 未绑定手机号",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 功能入口列表（每个都是独立卡片，圆角大、阴影克制、右箭头）
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileEntryCard(
                icon = Icons.Filled.Settings,
                iconBg = Color(0xFF28C76F),
                title = "通用设置",
                onClick = { /* TODO: 设置页 */ }
            )
            ProfileEntryCard(
                icon = Icons.Filled.Person,
                iconBg = Color(0xFF4BB3FF),
                title = "个人信息",
                onClick = { /* TODO: 个人资料页 */ }
            )
            ProfileEntryCard(
                icon = Icons.Filled.Security,
                iconBg = Color(0xFF8B5CFF),
                title = "安全选项",
                onClick = { /* TODO: 安全设置页 */ }
            )
            ProfileEntryCard(
                icon = Icons.Filled.HelpOutline,
                iconBg = Color(0xFFFFB020),
                title = "帮助与反馈",
                onClick = { /* TODO: 反馈/客服 */ }
            )
            ProfileEntryCard(
                icon = Icons.Filled.Info,
                iconBg = Color(0xFF9AA4B2),
                title = "关于云镜智联",
                onClick = { /* TODO: 关于页 */ }
            )
        }

        Spacer(Modifier.height(16.dp))

        // 账号操作区（切换身份/退出登录）
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                ProfileActionRow(
                    text = "切换身份",
                    onClick = { showSwitchConfirm = true }
                )
                Spacer(Modifier.height(10.dp))
                DangerLogoutButton(
                    text = "退出登录",
                    onClick = { showLogoutConfirm = true }
                )
            }
        }
    }

    // ✅ iOS 感：禁止点空白关闭（防误触）
    AppCenterDialog(
        visible = showSwitchConfirm,
        title = "切换身份",
        message = "将返回身份选择页，你可以重新选择买家或商家入口。",
        confirmText = "继续切换",
        cancelText = "取消",
        onConfirm = {
            showSwitchConfirm = false
            onSwitchRole()
        },
        onCancel = { showSwitchConfirm = false },
        dismissOnClickOutside = false
    )

    AppCenterDialog(
        visible = showLogoutConfirm,
        title = "退出登录",
        message = "退出后需要重新登录，确定要退出吗？",
        confirmText = "退出登录",
        cancelText = "取消",
        onConfirm = {
            showLogoutConfirm = false
            onLogout()
        },
        onCancel = { showLogoutConfirm = false },
        dismissOnClickOutside = false
    )
}

@Composable
private fun SimplePlaceholderPage(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(14.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(18.dp))
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier
                .fillMaxWidth()
                .padding(18.dp)) {
                Text("页面搭好啦", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "后续把真实功能接进来：列表、搜索、筛选、拍照入口等。",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ---------------------------
   iOS 风基础组件：背景 / 卡片 / 按钮
---------------------------- */

@Composable
private fun Modifier.buyerSoftBackground(): Modifier {
    return this.background(
        brush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            )
        )
    )
}

@Composable
private fun SoftCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                )
            )
            .pressClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun SecondaryPillButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .pressClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BuyerAvatar(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 先用一个“山”味的占位：后续你接真实头像/Logo，只需要换这里
        Text(
            text = "云",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ProfileEntryCard(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .pressClick(onClick = onClick), // ✅ 无 ripple + iOS press
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg.copy(alpha = 0.90f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ProfileActionRow(
    text: String,           // 按钮文字（原 title）
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary) // 蓝色背景，可根据需要自定义颜色
            .pressClick(onClick = onClick)                 // 保留原有点击效果
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center                 // 内容居中
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary      // 文字颜色（通常为白色）
        )
    }
}

@Composable
private fun DangerLogoutButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(Color(0xFFFF3B30).copy(alpha = 0.92f)) // iOS 红
            .pressClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}