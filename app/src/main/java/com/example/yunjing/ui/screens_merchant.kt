package com.example.yunjing.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.yunjing.data.AuthStore
import com.example.yunjing.data.UserRole
import com.example.yunjing.nav.Destinations
import androidx.compose.ui.platform.LocalContext

/**
 * 商家主壳：自绘底部栏，整体风格对齐 Buyer
 */

@Composable
fun MerchantMainShell(
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    val innerNav = rememberNavController()
    val tabs = remember { merchantTabs() }

    val context = LocalContext.current
    val authStore = remember(context) { AuthStore(context) }

    val username by authStore
        .accountFlow(UserRole.MERCHANT)
        .collectAsState(initial = "未登录")

    val navBackStackEntry by innerNav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxSize()
            .merchantSoftBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                MerchantTabNavHost(
                    nav = innerNav,
                    username = username,
                    onSwitchRole = onSwitchRole,
                    onLogout = onLogout
                )
            }

            MerchantBottomBar(
                tabs = tabs,
                currentDestination = currentDestination,
                onTabClick = { route ->
                    // 工作台，永远能回到根
                    if (route == Destinations.MERCHANT_DASH) {
                        innerNav.popBackStack(Destinations.MERCHANT_DASH, inclusive = false)
                    } else {
                        innerNav.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Destinations.MERCHANT_DASH) { saveState = true }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun MerchantTabNavHost(
    nav: NavHostController,
    username: String,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    NavHost(
        navController = nav,
        startDestination = Destinations.MERCHANT_DASH
    ) {
        composable(Destinations.MERCHANT_DASH) {
            MerchantDashboardScreen(
                onGoAssist = { nav.navigate(Destinations.MERCHANT_ASSIST) },
                onGoContent = { nav.navigate(Destinations.MERCHANT_CONTENT) }
            )
        }
        composable(Destinations.MERCHANT_CONTENT) { MerchantContentScreen() }
        composable(Destinations.MERCHANT_ASSIST) { MerchantAssistScreen() }
        composable(Destinations.MERCHANT_PROFILE) {
            MerchantProfileScreen(
                username = username,
                onSwitchRole = onSwitchRole,
                onLogout = onLogout
            )
        }
    }
}

/* ---------------------------
   Tab 定义 & 底部栏
---------------------------- */

private data class MerchantTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private fun merchantTabs(): List<MerchantTab> = listOf(
    MerchantTab(
        route = Destinations.MERCHANT_DASH,
        label = "工作台",
        icon = Icons.Filled.Dashboard
    ),
    MerchantTab(
        route = Destinations.MERCHANT_CONTENT,
        label = "内容库",
        icon = Icons.Filled.Inventory2
    ),
    MerchantTab(
        route = Destinations.MERCHANT_ASSIST,
        label = "协助",
        icon = Icons.Filled.SupportAgent
    ),
    MerchantTab(
        route = Destinations.MERCHANT_PROFILE,
        label = "我的",
        icon = Icons.Filled.Person
    )
)

@Composable
private fun MerchantBottomBar(
    tabs: List<MerchantTab>,
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
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { tab ->
                val selected = currentDestination
                    ?.hierarchy
                    ?.any { it.route == tab.route } == true

                MerchantBottomBarItem(
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
private fun MerchantBottomBarItem(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val targetAlpha = if (selected) 1f else 0.55f
    val alpha by animateFloatAsState(targetValue = targetAlpha, label = "mTabAlpha")

    val targetScale = if (selected) 1f else 0.98f
    val scale by animateFloatAsState(targetValue = targetScale, label = "mTabScale")

    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .width(78.dp)
            .clip(RoundedCornerShape(16.dp))
            .pressClick(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            Box(
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                }
            ) {
                Icon(imageVector = icon, contentDescription = null)
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
   商家 - 工作台
---------------------------- */

private data class MerchantQueueItem(
    val title: String,
    val subtitle: String
)

@Composable
private fun MerchantDashboardScreen(
    onGoAssist: () -> Unit,
    onGoContent: () -> Unit
) {
    val queue = remember {
        listOf(
            MerchantQueueItem("待处理求助 · 3", "AI 已整理关键信息，建议优先处理"),
            MerchantQueueItem("进行中会话 · 1", "来自：智能门锁 · 远程标注中"),
            MerchantQueueItem("内容待发布 · 2", "说明书解析完成，等待检查版本")
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
            text = "云镜智联 · 商家",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "处理求助、管理内容、发起远程协助",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        // 主 CTA：进入协助
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "快速接入协助",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "一键进入会话，查看 AI 整理的步骤与画面",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))

                PrimaryPillButton(
                    text = "进入远程协助",
                    onClick = onGoAssist
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "上传内容",
                desc = "说明书/PDF\n模型/教程",
                icon = Icons.Filled.UploadFile,
                onClick = onGoContent,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "数据看板",
                desc = "成功率/耗时\n退货关联",
                icon = Icons.Filled.Analytics,
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "概览",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(queue) { item ->
                    DashboardRow(
                        title = item.title,
                        subtitle = item.subtitle,
                        onClick = { /* TODO */ }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DashboardRow(
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
                fontWeight = FontWeight.SemiBold
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
   商家 - 内容库
---------------------------- */

private data class ContentItem(
    val title: String,
    val subtitle: String,
    val badge: String
)

@Composable
private fun MerchantContentScreen() {
    val items = remember {
        listOf(
            ContentItem("智能门锁 · 安装教程", "版本 v1.2 · 2 天前更新", "已发布"),
            ContentItem("书架 · 说明书 PDF", "版本 v1.0 · 刚上传", "待解析"),
            ContentItem("小家电 · 3D 模型", "版本 v0.9 · 待校验", "待审核")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Text("内容库", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "管理说明书/PDF/模型/教程版本与发布状态",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("快速操作", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniChip(
                        text = "上传",
                        icon = Icons.Filled.UploadFile,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    MiniChip(
                        text = "解析",
                        icon = Icons.Filled.Description,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text("最近内容", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(items) { item ->
                    ContentRow(
                        title = item.title,
                        subtitle = item.subtitle,
                        badge = item.badge,
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentRow(
    title: String,
    subtitle: String,
    badge: String,
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
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        StatusBadge(text = badge)
        Spacer(Modifier.width(8.dp))

        Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatusBadge(text: String) {
    val (bg, fg) = when (text) {
        "已发布" -> Color(0xFF28C76F).copy(alpha = 0.16f) to Color(0xFF28C76F)
        "待解析" -> Color(0xFFFFB020).copy(alpha = 0.18f) to Color(0xFFFFB020)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) to MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

/* ---------------------------
   商家 - 远程协助（iOS 风）
---------------------------- */

@Composable
private fun MerchantAssistScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Text("远程协助", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "接入会话、查看 AI 步骤、发送标注（后续接入）",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("会话接入", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "建议：先接入 AI 已转人工的求助，效率最高",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))

                PrimaryPillButton(
                    text = "进入待处理队列",
                    onClick = { /* TODO */ }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Text("提示", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "后续这里接入：房间列表、WebRTC 画面、标注数据协议与叠加渲染。",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/* ---------------------------
   商家 - 我的
---------------------------- */

@Composable
private fun MerchantProfileScreen(
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
            .merchantSoftBackground()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .padding(bottom = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "团队与设置",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(14.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MerchantAvatar(modifier = Modifier.size(84.dp))
                Spacer(Modifier.height(12.dp))

                Text(
                    text = username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "商家 · 团队账号",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileEntryCard(
                icon = Icons.Filled.Settings,
                iconBg = Color(0xFF28C76F),
                title = "通用设置",
                onClick = { /* TODO */ }
            )
            ProfileEntryCard(
                icon = Icons.Filled.Security,
                iconBg = Color(0xFF8B5CFF),
                title = "权限与安全",
                onClick = { /* TODO */ }
            )
            ProfileEntryCard(
                icon = Icons.Filled.HelpOutline,
                iconBg = Color(0xFFFFB020),
                title = "帮助与反馈",
                onClick = { /* TODO */ }
            )
            ProfileEntryCard(
                icon = Icons.Filled.Info,
                iconBg = Color(0xFF9AA4B2),
                title = "关于云镜智联",
                onClick = { /* TODO */ }
            )
        }

        Spacer(Modifier.height(16.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
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

    // 防误触，禁止点空白关闭
    ProfileConfirmDialogs(
        showSwitchConfirm = showSwitchConfirm,
        showLogoutConfirm = showLogoutConfirm,
        onDismissSwitch = { showSwitchConfirm = false },
        onDismissLogout = { showLogoutConfirm = false },
        onConfirmSwitch = {
            showSwitchConfirm = false
            onSwitchRole()
        },
        onConfirmLogout = {
            showLogoutConfirm = false
            onLogout()
        },
        roleName = "商家",
        username = username
    )
}

/* ---------------------------
   基础组件：背景 / 卡片 / 按钮 / 小组件
   （保持与 Buyer 同一套观感）
---------------------------- */

@Composable
private fun Modifier.merchantSoftBackground(): Modifier {
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
private fun QuickActionCard(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(modifier = modifier, corner = 24.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
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
private fun MiniChip(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .pressClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MerchantAvatar(
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
        Text(
            text = "匠",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
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
    val shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pressClick(onClick = onClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = shape
            ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = shape
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
                Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
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
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .pressClick(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
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
            .background(Color(0xFFFF3B30).copy(alpha = 0.92f))
            .pressClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Logout, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}