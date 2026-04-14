package com.example.yunjing.ui.buyer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.demo.picker.PickerUnityActivity
import com.example.yunjing.PortraitCaptureActivity
import com.example.yunjing.data.AuthStore
import com.example.yunjing.data.UserRole
import com.example.yunjing.nav.Destinations
import com.example.yunjing.ui.ProfileConfirmDialogs
import com.example.yunjing.ui.buyer.model.BuyerTutorialDto
import com.example.yunjing.ui.buyer.viewmodel.BuyerTutorialViewModel
import com.example.yunjing.ui.pressClick
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlin.math.max

/**
 * 买家主壳：自绘底部栏
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

    // 需要修改
    val buyerUserId = 1L

    val tutorials = buyerTutorialViewModel.tutorials.map { it.toBuyerTutorialUi() }
    var selectedTutorial by remember { mutableStateOf<BuyerTutorialUi?>(null) }



    LaunchedEffect(Unit) {
        buyerTutorialViewModel.loadTutorials(buyerUserId)
    }

    LaunchedEffect(buyerTutorialViewModel.errorMessage) {
        buyerTutorialViewModel.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            buyerTutorialViewModel.clearError()
        }
    }

    fun handleScannedContent(rawText: String?) {
        if (rawText.isNullOrBlank()) {
            Toast.makeText(context, "未识别到二维码内容", Toast.LENGTH_SHORT).show()
            return
        }

        val publishCode = extractPublishCodeFromScan(rawText)
        if (publishCode.isNullOrBlank()) {
            Toast.makeText(context, "二维码内容无法识别", Toast.LENGTH_SHORT).show()
            return
        }

        buyerTutorialViewModel.importTutorial(
            publishCode = publishCode,
            buyerUserId = buyerUserId,
            onSuccess = {
                buyerTutorialViewModel.loadTutorials(buyerUserId)
                Toast.makeText(context, "已加入“我的教程”", Toast.LENGTH_SHORT).show()
                innerNav.navigate(Destinations.BUYER_TUTORIAL) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(Destinations.BUYER_HOME) { saveState = true }
                }
            }
        )
    }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents.isNullOrBlank()) {
            Toast.makeText(context, "已取消扫码", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "未选择图片", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        decodeQrFromImageUriWithMlKit(context, uri) { qrText ->
            if (qrText.isNullOrBlank()) {
                Toast.makeText(context, "这张图片中未识别到二维码", Toast.LENGTH_SHORT).show()
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
                    onPrimaryScan = { launchQrScanner() },
                    onPickLocalQrImage = { launchLocalQrImagePicker() },
                    onDeleteTutorial = { tutorial ->
                        buyerTutorialViewModel.deleteTutorial(
                            tutorialId = tutorial.id,
                            buyerUserId = buyerUserId,
                            onSuccess = {
                                Toast.makeText(context, "教程已移除", Toast.LENGTH_SHORT).show()
                                selectedTutorial = null
                                innerNav.popBackStack()
                            }
                        )
                    },
                    onSelectTutorial = { tutorial ->
                        selectedTutorial = tutorial
                        innerNav.navigate("buyer_tutorial_detail")
                    },
                    onBackTutorialDetail = { innerNav.popBackStack() }
                )
            }

            if (!hideBottomBar) {
                BuyerBottomBar(
                    tabs = tabs,
                    currentDestination = currentDestination,
                    onTabClick = { route ->
                        if (route == Destinations.BUYER_HOME) {
                            innerNav.popBackStack(Destinations.BUYER_HOME, inclusive = false)
                        } else {
                            innerNav.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(Destinations.BUYER_HOME) {
                                    saveState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
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

        composable("buyer_tutorial_detail") {
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
                videos = buyerAiMockVideos(),
                onBack = { nav.popBackStack() },
                onSelectVideo = { video ->
                    nav.navigate("$BUYER_AI_CALL/${video.id}")
                }
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
            val video = buyerAiMockVideos().firstOrNull { it.id == videoId }

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
                    videos = buyerAiMockVideos(),
                    onBack = { nav.popBackStack() },
                    onSelectVideo = { selected ->
                        nav.navigate("$BUYER_AI_CALL/${selected.id}")
                    }
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

/* ---------------------------
   Tab 定义 & 底部栏
---------------------------- */

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

/* ---------------------------
   买家首页
---------------------------- */

private const val BUYER_TUTORIAL_DETAIL = "buyer_tutorial_detail"
private const val BUYER_AI_PICK = "buyer_ai_pick"
private const val BUYER_AI_CALL = "buyer_ai_call"
private const val BUYER_AI_CALL_ARG = "videoId"

data class BuyerTutorialItem(
    val localId: Long,
    val publishCode: String,
    val title: String,
    val subtitle: String,
    val explodedImageUrl: String?,
    val tutorialVideoUrl: String?,
    val tutorialTitle: String?,
    val publishUrl: String?,
    val addedAtLabel: String
)

private data class BuyerTutorialUi(
    val id: Long,
    val publishCode: String,
    val tutorialName: String,
    val projectDesc: String?,
    val explodedImageUrl: String?,
    val tutorialVideoUrl: String?,
    val tutorialTitle: String?,
    val publishUrl: String?,
    val addedAtText: String
)

private data class RecentItem(
    val tutorialId: Long,
    val title: String,
    val subtitle: String
)
@Composable
private fun BuyerHomeScreen(
    onPrimaryScan: () -> Unit,
    onQuickAi: () -> Unit,
    onQuickTutorial: () -> Unit,
    recentTutorials: List<BuyerTutorialUi>,
    onOpenTutorial: (BuyerTutorialUi) -> Unit
) {
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
            text = "扫码导入商家发布项目，加入“我的教程”后可持续查看",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "开始安装",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "扫描商品二维码，将项目加入“我的教程”，并可查看爆炸图、打开教程播放器",
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
                title = "我的教程",
                desc = "查看已导入教程\n继续学习",
                onClick = onQuickTutorial,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "最近导入",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))

        if (recentTutorials.isEmpty()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text("暂无教程", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "点击上方“扫码开始”，把商家发布的项目导入到“我的教程”。",
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
                    items(recentTutorials, key = { it.id }) { item ->
                        RecentRow(
                            title = item.tutorialName,
                            subtitle = item.addedAtText,
                            onClick = { onOpenTutorial(item) }
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
   其他 Tab（先给占位，后续再接真实功能）
---------------------------- */

@Composable
private fun BuyerTutorialScreen(
    tutorials: List<BuyerTutorialUi>,
    onScanImport: () -> Unit,
    onPickLocalQrImage: () -> Unit,
    onOpenTutorial: (BuyerTutorialUi) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Text("我的教程", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "扫码导入后的项目会显示在这里，点击后可查看爆炸图和教程播放器",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        PrimaryPillButton(
            text = "扫码导入教程",
            onClick = onScanImport
        )

        Spacer(Modifier.height(10.dp))

        PrimaryPillButton(
            text = "从相册识别二维码",
            onClick = onPickLocalQrImage
        )

        Spacer(Modifier.height(16.dp))

        if (tutorials.isEmpty()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text("暂无已导入教程", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "商家发布后，买家扫码即可加入“我的教程”。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tutorials, key = { it.id }) { item ->
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pressClick { onOpenTutorial(item) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.tutorialName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    item.projectDesc ?: "点击查看教程详情",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    item.addedAtText,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
private fun openBuyerUnityPlayer(
    context: Context,
    tutorial: BuyerTutorialUi
) {
    try {
        val intent = Intent(context, PickerUnityActivity::class.java).apply {
            putExtra("publishCode", tutorial.publishCode)
            putExtra("tutorialVideoUrl", tutorial.tutorialVideoUrl)
            putExtra("tutorialTitle", tutorial.tutorialTitle)
        }
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "未找到 Unity 页面", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "打开 Unity 失败：${e.message ?: "未知错误"}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun normalizeBuyerPreviewUrl(rawUrl: String?): String? {
    if (rawUrl.isNullOrBlank()) return null

    return when {
        rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> {
            rawUrl.replace("localhost", "10.0.2.2")
//            rawUrl.replace("localhost", "172.20.10.3")
        }

        rawUrl.startsWith("/") -> {
            "http://10.0.2.2:8080$rawUrl"
//            "http://172.20.10.3:8080$rawUrl"
        }
        else -> rawUrl
    }
}

@Composable
private fun BuyerZoomableImageViewer(
    imageUrl: String,
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 4f
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            reset()
                        } else {
                            scale = 2f
                            offset = Offset.Zero
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                    if (newScale <= 1f) {
                        scale = 1f
                        offset = Offset.Zero
                        return@detectTransformGestures
                    }

                    scale = newScale
                    offset += pan

                    val maxX = max(0f, containerSize.width * (scale - 1f) / 2f)
                    val maxY = max(0f, containerSize.height * (scale - 1f) / 2f)

                    offset = Offset(
                        x = offset.x.coerceIn(-maxX, maxX),
                        y = offset.y.coerceIn(-maxY, maxY)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "爆炸图全屏预览",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun BuyerExplodedImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
        ) {
            BuyerZoomableImageViewer(
                imageUrl = imageUrl,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "关闭",
                    color = Color.White,
                    modifier = Modifier.pressClick(onClick = onDismiss)
                )
            }
        }
    }
}

@Composable
private fun BuyerTutorialRow(
    item: BuyerTutorialItem,
    onClick: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.addedAtLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "›",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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

        // 顶部标题
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

        // 头像 + 昵称区
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

        // 功能入口列表
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

    // 禁止点空白关闭（防误触）
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
        roleName = "买家",
        username = username
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
        // 先占位：后续接真实头像/Logo，只需要换这里
        Text(
            text = "云",
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
            // Surface 本身会按 shape 裁切与绘制，不需要再额外 clip 一次
            .pressClick(onClick = onClick)
            // 用“轻描边”替代阴影
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = shape
            ),
        tonalElevation = 0.dp,   // 关掉 tonal（避免表面色调变化）
        shadowElevation = 0.dp,  // 关键：关掉阴影
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
    text: String, // 按钮文字
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary) // 蓝色背景，可根据需要自定义颜色
            .pressClick(onClick = onClick) // 保留原有点击效果
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center // 内容居中
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary // 文字颜色
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

@Composable
private fun BuyerTutorialDetailScreen(
    tutorial: BuyerTutorialUi?,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var previewExploded by remember { mutableStateOf(false) }

    if (tutorial == null) {
        SimplePlaceholderPage(
            title = "教程详情",
            subtitle = "未找到教程内容"
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        PrimaryPillButton(
            text = "返回教程列表",
            onClick = onBack
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = tutorial.tutorialName,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = tutorial.projectDesc ?: "教程详情",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (!tutorial.explodedImageUrl.isNullOrBlank()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("爆炸图", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = tutorial.explodedImageUrl,
                        contentDescription = "爆炸图缩略图",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryPillButton(
                        text = "查看爆炸图",
                        onClick = { previewExploded = true }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        if (!tutorial.tutorialVideoUrl.isNullOrBlank()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = tutorial.tutorialTitle ?: "教程播放器",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "点击后可打开 Unity 教程播放器",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    PrimaryPillButton(
                        text = "打开教程播放器",
                        onClick = {
                            try {
                                val intent = Intent(context, PickerUnityActivity::class.java).apply {
                                    putExtra("publishCode", tutorial.publishCode)
                                    putExtra("tutorialVideoUrl", tutorial.tutorialVideoUrl)
                                    putExtra("tutorialTitle", tutorial.tutorialTitle)
                                }
                                context.startActivity(intent)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, "未找到 Unity 页面", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "打开 Unity 失败：${e.message ?: "未知错误"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
        }

        DangerDeleteButton(
            text = "移除教程",
            onClick = onDelete
        )
    }

    if (previewExploded && !tutorial.explodedImageUrl.isNullOrBlank()) {
        BuyerExplodedImagePreviewDialog(
            imageUrl = tutorial.explodedImageUrl,
            onDismiss = { previewExploded = false }
        )
    }
}

private fun extractPublishCodeFromScan(raw: String?): String? {
    val value = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    // 1) 如果本身就是 publishCode
    val simpleCodeRegex = Regex("^[A-Za-z0-9]{16,64}$")
    if (simpleCodeRegex.matches(value)) {
        return value
    }

    // 2) 如果是 URL，取最后一段 path
    runCatching {
        val uri = Uri.parse(value)
        val lastSegment = uri.lastPathSegment
        if (!lastSegment.isNullOrBlank() && simpleCodeRegex.matches(lastSegment)) {
            return lastSegment
        }

        // 3) 如果是 query 参数 code
        val code = uri.getQueryParameter("code")
        if (!code.isNullOrBlank() && simpleCodeRegex.matches(code)) {
            return code
        }
    }

    return null
}

private fun BuyerTutorialDto.toBuyerTutorialUi(): BuyerTutorialUi {
    return BuyerTutorialUi(
        id = id,
        publishCode = publishCode,
        tutorialName = tutorialName,
        projectDesc = projectDesc,
        explodedImageUrl = normalizeBuyerPreviewUrl(explodedImageUrl),
        tutorialVideoUrl = tutorialVideoUrl,
        tutorialTitle = tutorialTitle,
        publishUrl = publishUrl,
        addedAtText = addedAt ?: "刚刚导入"
    )
}

private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}

private fun decodeQrFromBitmap(bitmap: Bitmap): String? {
    val candidates = buildBitmapCandidates(bitmap)

    for (candidate in candidates) {
        val text = tryDecodeBitmap(candidate)
        if (!text.isNullOrBlank()) {
            return text
        }
    }
    return null
}

private fun tryDecodeBitmap(bitmap: Bitmap): String? {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    return try {
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val hints = mapOf(
            com.google.zxing.DecodeHintType.TRY_HARDER to true,
            com.google.zxing.DecodeHintType.POSSIBLE_FORMATS to listOf(
                com.google.zxing.BarcodeFormat.QR_CODE
            )
        )

        MultiFormatReader().decode(binaryBitmap, hints).text
    } catch (e: Exception) {
        null
    }
}

private fun buildBitmapCandidates(src: Bitmap): List<Bitmap> {
    val result = mutableListOf<Bitmap>()
    result.add(src)

    // 中心裁剪：二维码通常在截图中间区域
    val centerCrop = cropCenter(src, 0.7f)
    if (centerCrop != null) result.add(centerCrop)

    val tighterCenterCrop = cropCenter(src, 0.5f)
    if (tighterCenterCrop != null) result.add(tighterCenterCrop)

    // 旋转重试
    val baseList = result.toList()
    for (bmp in baseList) {
        result.add(rotateBitmap(bmp, 90f))
        result.add(rotateBitmap(bmp, 180f))
        result.add(rotateBitmap(bmp, 270f))
    }

    return result
}

private fun cropCenter(src: Bitmap, ratio: Float): Bitmap? {
    if (ratio <= 0f || ratio > 1f) return null

    val cropWidth = (src.width * ratio).toInt()
    val cropHeight = (src.height * ratio).toInt()
    if (cropWidth <= 0 || cropHeight <= 0) return null

    val left = (src.width - cropWidth) / 2
    val top = (src.height - cropHeight) / 2

    return try {
        Bitmap.createBitmap(src, left, top, cropWidth, cropHeight)
    } catch (e: Exception) {
        null
    }
}

private fun rotateBitmap(src: Bitmap, degrees: Float): Bitmap {
    val matrix = android.graphics.Matrix().apply {
        postRotate(degrees)
    }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
}

private fun decodeQrFromImageUri(context: android.content.Context, uri: Uri): String? {
    return try {
        val bitmap = loadBitmapFromUri(context, uri) ?: return null
        decodeQrFromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun decodeQrFromImageUriWithMlKit(
    context: android.content.Context,
    uri: Uri,
    onResult: (String?) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)

        val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstOrNull()?.rawValue
                onResult(rawValue)
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult(null)
            }
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(null)
    }
}

@Composable
private fun DangerDeleteButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD92D20),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}