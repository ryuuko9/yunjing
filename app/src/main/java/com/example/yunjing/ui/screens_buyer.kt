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
                    onLogout = onLogout
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
    onLogout: () -> Unit
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
                onLogout = onLogout
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
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    var showSwitchConfirm by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Text(
            text = "我的",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "账号、角色与设置",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
                Text("当前身份：买家", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "你可以切换身份进入商家端，或退出登录。",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(14.dp))
                PrimaryPillButton(text = "切换身份", onClick = { showSwitchConfirm = true })
                Spacer(Modifier.height(10.dp))
                SecondaryPillButton(text = "退出登录", onClick = { showLogoutConfirm = true })
            }
        }
    }

    AppConfirmDialog(
        visible = showSwitchConfirm,
        title = "切换身份",
        message = "将返回身份选择页。你可以重新选择入口。",
        confirmText = "继续切换",
        cancelText = "取消",
        onConfirm = {
            showSwitchConfirm = false
            onSwitchRole()
        },
        onCancel = { showSwitchConfirm = false }
    )

    AppConfirmDialog(
        visible = showLogoutConfirm,
        title = "退出登录",
        message = "退出后需要重新登录。",
        confirmText = "退出登录",
        cancelText = "取消",
        onConfirm = {
            showLogoutConfirm = false
            onLogout()
        },
        onCancel = { showLogoutConfirm = false }
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