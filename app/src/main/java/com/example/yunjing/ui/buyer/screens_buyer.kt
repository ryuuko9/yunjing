package com.example.yunjing.ui.buyer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.yunjing.PortraitCaptureActivity
import com.example.yunjing.data.AuthStore
import com.example.yunjing.data.UserRole
import com.example.yunjing.nav.Destinations
import com.example.yunjing.ui.buyer.viewmodel.BuyerTutorialViewModel
import com.example.yunjing.ui.pressClick
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * 文件作用：
 * 负责 buyer 包的主壳结构、内部导航、扫码导入流程和底部 Tab 切换，
 * 让页面内容与导航外壳彻底分离。
 */

/**
 * 买家主壳：自绘底部栏
 */

/**
 * 作用：
 * 作为买家端入口壳体，负责初始化教程数据、处理扫码导入，以及组装内部导航和底部栏。
 */
@Composable
fun BuyerMainShell(
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    buyerTutorialViewModel: BuyerTutorialViewModel
) {
    val innerNav = rememberNavController()
    val tabs = remember { buyerTabs() }

    val context = LocalContext.current
    val authStore = remember(context) { AuthStore(context) }

    val username by authStore
        .accountFlow(UserRole.BUYER)
        .collectAsState(initial = "未登录")

    val buyerUserId by authStore
        .userIdFlow(UserRole.BUYER)
        .collectAsState(initial = null)

    val tutorials = buyerTutorialViewModel.tutorials.map { it.toBuyerTutorialUi() }
    var selectedTutorial by remember { mutableStateOf<BuyerTutorialUi?>(null) }
    val missingBuyerIdMessage = "当前买家账号缺少用户 ID，请重新登录"
    val openTutorialDetail: (BuyerTutorialUi) -> Unit = { tutorial ->
        selectedTutorial = tutorial
        innerNav.navigate(BuyerTutorialDetailRoute)
    }
    val removeTutorial: (BuyerTutorialUi) -> Unit = removeTutorial@{ tutorial ->
        val currentBuyerUserId = buyerUserId
        if (currentBuyerUserId == null) {
            context.showShortToast(missingBuyerIdMessage)
            return@removeTutorial
        }
        buyerTutorialViewModel.deleteTutorial(
            tutorialId = tutorial.id,
            buyerUserId = currentBuyerUserId,
            onSuccess = {
                context.showShortToast("教程已移除")
                selectedTutorial = null
                innerNav.popBackStack()
            }
        )
    }

    LaunchedEffect(buyerUserId) {
        val currentBuyerUserId = buyerUserId ?: return@LaunchedEffect
        buyerTutorialViewModel.loadTutorials(currentBuyerUserId)
    }

    LaunchedEffect(buyerTutorialViewModel.errorMessage) {
        buyerTutorialViewModel.errorMessage?.let { message ->
            context.showShortToast(message)
            buyerTutorialViewModel.clearError()
        }
    }

    fun handleScannedContent(rawText: String?) {
        if (rawText.isNullOrBlank()) {
            context.showShortToast("未识别到二维码内容")
            return
        }

        val publishCode = extractPublishCodeFromScan(rawText)
        if (publishCode.isNullOrBlank()) {
            context.showShortToast("二维码内容无法识别")
            return
        }

        val currentBuyerUserId = buyerUserId
        if (currentBuyerUserId == null) {
            context.showShortToast(missingBuyerIdMessage)
            return
        }

        buyerTutorialViewModel.importTutorial(
            publishCode = publishCode,
            buyerUserId = currentBuyerUserId,
            onSuccess = {
                buyerTutorialViewModel.loadTutorials(currentBuyerUserId)
                context.showShortToast("已加入“我的教程”")
                innerNav.navigateToBuyerTab(Destinations.BUYER_TUTORIAL)
            }
        )
    }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents.isNullOrBlank()) {
            context.showShortToast("已取消扫码")
            return@rememberLauncherForActivityResult
        }
        handleScannedContent(result.contents)
    }

    fun launchQrScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("请扫描商家发布的二维码")
            setBeepEnabled(true)
            setOrientationLocked(true)
            setBarcodeImageEnabled(false)
            setCaptureActivity(PortraitCaptureActivity::class.java)
        }
        scanLauncher.launch(options)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            context.showShortToast("未选择图片")
            return@rememberLauncherForActivityResult
        }

        decodeQrFromImageUriWithMlKit(context, uri) { qrText ->
            if (qrText.isNullOrBlank()) {
                context.showShortToast("这张图片中未识别到二维码")
                return@decodeQrFromImageUriWithMlKit
            }

            handleScannedContent(qrText)
        }
    }

    fun launchLocalQrImagePicker() {
        imagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val navBackStackEntry by innerNav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val hideBottomBar = currentRoute?.startsWith("$BUYER_AI_CALL/") == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .buyerSoftBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                BuyerTabNavHost(
                    nav = innerNav,
                    onSwitchRole = onSwitchRole,
                    onLogout = onLogout,
                    username = username,
                    tutorials = tutorials,
                    selectedTutorial = selectedTutorial,
                    onPrimaryScan = ::launchQrScanner,
                    onPickLocalQrImage = ::launchLocalQrImagePicker,
                    onDeleteTutorial = removeTutorial,
                    onSelectTutorial = openTutorialDetail,
                    onBackTutorialDetail = { innerNav.popBackStack() }
                )
            }

            if (!hideBottomBar) {
                BuyerBottomBar(
                    tabs = tabs,
                    currentDestination = currentDestination,
                    onTabClick = innerNav::navigateToBuyerTab
                )
            }
        }
    }
}

/**
 * 作用：
 * 管理买家端内部页面导航，并把不同页面组装到同一个 NavHost 中。
 */
@Composable
private fun BuyerTabNavHost(
    nav: NavHostController,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    username: String,
    tutorials: List<BuyerTutorialUi>,
    selectedTutorial: BuyerTutorialUi?,
    onPrimaryScan: () -> Unit,
    onPickLocalQrImage: () -> Unit,
    onSelectTutorial: (BuyerTutorialUi) -> Unit,
    onDeleteTutorial: (BuyerTutorialUi) -> Unit,
    onBackTutorialDetail: () -> Unit
) {
    val aiVideos = remember { buyerAiDemoVideos() }
    val openAiVideo: (BuyerAiDemoVideo) -> Unit = { video ->
        nav.navigate(buyerAiCallRoute(video.id))
    }

    NavHost(
        navController = nav,
        startDestination = Destinations.BUYER_HOME
    ) {
        composable(Destinations.BUYER_HOME) {
            BuyerHomeScreen(
                onPrimaryScan = onPrimaryScan,
                onQuickAi = { nav.navigate(Destinations.BUYER_AI) },
                onQuickTutorial = { nav.navigate(Destinations.BUYER_TUTORIAL) },
                recentTutorials = tutorials.take(3),
                onOpenTutorial = onSelectTutorial
            )
        }

        composable(Destinations.BUYER_TUTORIAL) {
            BuyerTutorialScreen(
                tutorials = tutorials,
                onScanImport = onPrimaryScan,
                onPickLocalQrImage = onPickLocalQrImage,
                onOpenTutorial = onSelectTutorial
            )
        }

        composable(BuyerTutorialDetailRoute) {
            BuyerTutorialDetailScreen(
                tutorial = selectedTutorial,
                onBack = onBackTutorialDetail,
                onDelete = {
                    selectedTutorial?.let { onDeleteTutorial(it) }
                }
            )
        }

        composable(Destinations.BUYER_AI) {
            BuyerAiRemoteAssistHome(
                onEnterAi = {
                    nav.navigate(BUYER_AI_PICK)
                }
            )
        }

        composable(BUYER_AI_PICK) {
            BuyerAiVideoSelectScreen(
                videos = aiVideos,
                onBack = { nav.popBackStack() },
                onSelectVideo = openAiVideo
            )
        }

        composable(
            route = "$BUYER_AI_CALL/{$BUYER_AI_CALL_ARG}",
            arguments = listOf(
                navArgument(BUYER_AI_CALL_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString(BUYER_AI_CALL_ARG)
            val video = aiVideos.firstOrNull { it.id == videoId }

            if (video != null) {
                BuyerAiCallScreen(
                    video = video,
                    onExit = { nav.popBackStack() },
                    onTransferHuman = {
                        nav.popBackStack(BUYER_AI_PICK, inclusive = false)
                    }
                )
            } else {
                BuyerAiVideoSelectScreen(
                    videos = aiVideos,
                    onBack = { nav.popBackStack() },
                    onSelectVideo = openAiVideo
                )
            }
        }

        composable(Destinations.BUYER_PROFILE) {
            BuyerProfileScreen(
                onSwitchRole = onSwitchRole,
                onLogout = onLogout,
                username = username
            )
        }
    }
}

/**
 * Tab 定义 & 底部栏
 */

/**
 * 作用：
 * 描述底部 Tab 的基础信息。
 */
private data class BuyerTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * 作用：
 * 提供买家端底部导航的固定 Tab 列表。
 */
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
 * 作用：
 * 渲染买家端底部导航栏，并根据当前路由高亮选中项。
 */
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

/**
 * 作用：
 * 渲染底部导航中的单个按钮项。
 */
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
            .pressClick(onClick = onClick)
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

/**
 * 作用：
 * 生成 AI 通话页的目标路由。
 */
private fun buyerAiCallRoute(videoId: String): String = "$BUYER_AI_CALL/$videoId"

/**
 * 作用：
 * 统一处理底部 Tab 导航跳转与首页回栈逻辑。
 */
private fun NavHostController.navigateToBuyerTab(route: String) {
    if (route == Destinations.BUYER_HOME) {
        popBackStack(Destinations.BUYER_HOME, inclusive = false)
        return
    }

    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Destinations.BUYER_HOME) {
            saveState = true
        }
    }
}

private const val BUYER_AI_PICK = "buyer_ai_pick"
private const val BUYER_AI_CALL = "buyer_ai_call"
private const val BUYER_AI_CALL_ARG = "videoId"
private const val BuyerTutorialDetailRoute = "buyer_tutorial_detail"
