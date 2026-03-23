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
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.core.content.ContextCompat
import android.widget.ImageView
import android.widget.VideoView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.viewinterop.AndroidView

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

private enum class PendingMediaType {
    IMAGE, VIDEO
}

private data class PendingUploadItem(
    val uri: Uri,
    val type: PendingMediaType,
    val name: String,
    val source: String
)

@Composable
private fun MerchantContentScreen() {
    val context = LocalContext.current

    val items = remember {
        listOf(
            ContentItem("智能门锁 · 安装教程", "版本 v1.2 · 2 天前更新", "已发布"),
            ContentItem("书架 · 说明书 PDF", "版本 v1.0 · 刚上传", "待解析"),
            ContentItem("小家电 · 3D 模型", "版本 v0.9 · 待校验", "待审核")
        )
    }

    // 弹窗状态
    var showContinueAddSheet by remember { mutableStateOf(false) }
    var showEntrySheet by remember { mutableStateOf(false) }
    var showPickTypeSheet by remember { mutableStateOf(false) }
    var showCaptureTypeSheet by remember { mutableStateOf(false) }

    // 预览状态
    var previewItem by remember { mutableStateOf<PendingUploadItem?>(null) }

    // 批次状态
    var pendingUploads by remember { mutableStateOf(listOf<PendingUploadItem>()) }
    var activeUploadType by remember { mutableStateOf<PendingMediaType?>(null) }
    var isUploadSessionActive by remember { mutableStateOf(false) }

    // 拍摄缓存 Uri
    var currentCaptureImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentCaptureVideoUri by remember { mutableStateOf<Uri?>(null) }

    // 权限通过后动作
    var pendingCameraAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun appendPickedItems(uris: List<Uri>, type: PendingMediaType, source: String) {
        if (uris.isEmpty()) return

        val startIndex = pendingUploads.size
        val newItems = uris.mapIndexed { index, uri ->
            PendingUploadItem(
                uri = uri,
                type = type,
                name = "${if (type == PendingMediaType.IMAGE) "图片" else "视频"} ${startIndex + index + 1}",
                source = source
            )
        }
        pendingUploads = pendingUploads + newItems
    }

    fun startUploadSession(type: PendingMediaType) {
        if (activeUploadType == null) {
            activeUploadType = type
            isUploadSessionActive = true
            pendingUploads = emptyList()
        }
    }

    fun canAddType(type: PendingMediaType): Boolean {
        return activeUploadType == null || activeUploadType == type
    }

    fun resetBatch() {
        pendingUploads = emptyList()
        activeUploadType = null
        isUploadSessionActive = false
        showContinueAddSheet = false
        showPickTypeSheet = false
        showCaptureTypeSheet = false
        showEntrySheet = false
        previewItem = null
    }

    // 相机权限
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingCameraAction?.invoke()
            pendingCameraAction = null
        } else {
            Toast.makeText(context, "未授予相机权限，无法拍摄", Toast.LENGTH_SHORT).show()
        }
    }

    fun ensureCameraPermission(onGranted: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            onGranted()
        } else {
            pendingCameraAction = onGranted
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 多选图片
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendPickedItems(
            uris = uris,
            type = PendingMediaType.IMAGE,
            source = "本地选择"
        )
    }

    // 多选视频
    val pickVideosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendPickedItems(
            uris = uris,
            type = PendingMediaType.VIDEO,
            source = "本地选择"
        )
    }

    // 拍照
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCaptureImageUri != null) {
            appendPickedItems(
                uris = listOf(currentCaptureImageUri!!),
                type = PendingMediaType.IMAGE,
                source = "拍摄"
            )
        }
    }

    // 录视频
    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && currentCaptureVideoUri != null) {
            appendPickedItems(
                uris = listOf(currentCaptureVideoUri!!),
                type = PendingMediaType.VIDEO,
                source = "拍摄"
            )
        }
    }

    fun launchTakePhoto() {
        ensureCameraPermission {
            val uri = createImageUri(context)
            currentCaptureImageUri = uri
            takePhotoLauncher.launch(uri)
        }
    }

    fun launchTakeVideo() {
        ensureCameraPermission {
            val uri = createVideoUri(context)
            currentCaptureVideoUri = uri
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
            takeVideoLauncher.launch(intent)
        }
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
                        onClick = {
                            if (pendingUploads.isNotEmpty() && !isUploadSessionActive) {
                                Toast.makeText(
                                    context,
                                    "当前批次已结束，请先解析或清空后再开始新批次",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showEntrySheet = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MiniChip(
                        text = "解析",
                        icon = Icons.Filled.Description,
                        onClick = {
                            if (pendingUploads.isEmpty()) {
                                Toast.makeText(context, "请先上传照片或视频", Toast.LENGTH_SHORT).show()
                            } else if (isUploadSessionActive) {
                                Toast.makeText(context, "请先点击“结束上传”再解析", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "进入解析流程（待接后续逻辑）", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (pendingUploads.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))

            Text(
                text = buildString {
                    append("当前批次：")
                    append(if (activeUploadType == PendingMediaType.IMAGE) "照片" else "视频")
                    append(" · 共 ${pendingUploads.size} 项")
                    append(if (isUploadSessionActive) " · 上传中" else " · 待解析")
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            if (isUploadSessionActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniChip(
                        text = "继续添加",
                        icon = if (activeUploadType == PendingMediaType.IMAGE) {
                            Icons.Filled.Image
                        } else {
                            Icons.Filled.Videocam
                        },
                        onClick = { showContinueAddSheet = true },
                        modifier = Modifier.weight(1f)
                    )

                    MiniChip(
                        text = "结束上传",
                        icon = Icons.Filled.CheckCircle,
                        onClick = {
                            isUploadSessionActive = false
                            showContinueAddSheet = false
                            Toast.makeText(
                                context,
                                "当前批次已结束，请进行解析",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))
            }

            PrimaryPillButton(
                text = "清空当前批次",
                onClick = {
                    resetBatch()
                    Toast.makeText(context, "当前批次已清空", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(Modifier.height(12.dp))

            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pendingUploads.forEachIndexed { index, item ->
                        PendingUploadRow(
                            item = item,
                            onPreview = { previewItem = item },
                            onRemove = {
                                val mutable = pendingUploads.toMutableList()
                                mutable.removeAt(index)
                                pendingUploads = mutable.mapIndexed { newIndex, old ->
                                    old.copy(
                                        name = "${if (old.type == PendingMediaType.IMAGE) "图片" else "视频"} ${newIndex + 1}"
                                    )
                                }

                                if (pendingUploads.isEmpty()) {
                                    activeUploadType = null
                                    isUploadSessionActive = false
                                    previewItem = null
                                }
                            }
                        )
                    }
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

    // 一级弹窗：上传 / 拍摄
    if (showEntrySheet) {
        UploadEntrySheet(
            onDismiss = { showEntrySheet = false },
            onUploadClick = {
                showEntrySheet = false
                showPickTypeSheet = true
            },
            onCaptureClick = {
                showEntrySheet = false
                showCaptureTypeSheet = true
            }
        )
    }

    // 二级弹窗：选择上传类型
    if (showPickTypeSheet) {
        UploadTypeSheet(
            title = "选择上传类型",
            onDismiss = { showPickTypeSheet = false },
            onImageClick = {
                if (!canAddType(PendingMediaType.IMAGE)) {
                    Toast.makeText(context, "当前批次只能上传视频，请先清空当前批次", Toast.LENGTH_SHORT).show()
                    return@UploadTypeSheet
                }
                startUploadSession(PendingMediaType.IMAGE)
                showPickTypeSheet = false
                pickImagesLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onVideoClick = {
                if (!canAddType(PendingMediaType.VIDEO)) {
                    Toast.makeText(context, "当前批次只能上传图片，请先清空当前批次", Toast.LENGTH_SHORT).show()
                    return@UploadTypeSheet
                }
                startUploadSession(PendingMediaType.VIDEO)
                showPickTypeSheet = false
                pickVideosLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            }
        )
    }

    // 二级弹窗：选择拍摄类型
    if (showCaptureTypeSheet) {
        UploadTypeSheet(
            title = "选择拍摄类型",
            onDismiss = { showCaptureTypeSheet = false },
            onImageClick = {
                if (!canAddType(PendingMediaType.IMAGE)) {
                    Toast.makeText(context, "当前批次只能上传视频，请先清空当前批次", Toast.LENGTH_SHORT).show()
                    return@UploadTypeSheet
                }
                startUploadSession(PendingMediaType.IMAGE)
                showCaptureTypeSheet = false
                launchTakePhoto()
            },
            onVideoClick = {
                if (!canAddType(PendingMediaType.VIDEO)) {
                    Toast.makeText(context, "当前批次只能上传图片，请先清空当前批次", Toast.LENGTH_SHORT).show()
                    return@UploadTypeSheet
                }
                startUploadSession(PendingMediaType.VIDEO)
                showCaptureTypeSheet = false
                launchTakeVideo()
            }
        )
    }

    // 继续添加
    if (showContinueAddSheet && activeUploadType != null && isUploadSessionActive) {
        ContinueAddSheet(
            type = activeUploadType!!,
            onDismiss = { showContinueAddSheet = false },
            onPickClick = {
                showContinueAddSheet = false
                if (activeUploadType == PendingMediaType.IMAGE) {
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    pickVideosLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            },
            onCaptureClick = {
                showContinueAddSheet = false
                if (activeUploadType == PendingMediaType.IMAGE) {
                    launchTakePhoto()
                } else {
                    launchTakeVideo()
                }
            }
        )
    }

    // 媒体预览
    if (previewItem != null) {
        MediaPreviewSheet(
            item = previewItem!!,
            onDismiss = { previewItem = null }
        )
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
private fun PendingUploadRow(
    item: PendingUploadItem,
    onPreview: () -> Unit,
    onRemove: () -> Unit
) {
    val icon = if (item.type == PendingMediaType.IMAGE) {
        Icons.Filled.Image
    } else {
        Icons.Filled.Videocam
    }

    val typeText = if (item.type == PendingMediaType.IMAGE) "图片" else "视频"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$typeText · ${item.source}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onPreview) {
            Text("预览")
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFF3B30).copy(alpha = 0.12f))
                .pressClick(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "删除",
                tint = Color(0xFFFF3B30)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaPreviewSheet(
    item: PendingUploadItem,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (item.type == PendingMediaType.IMAGE) "图片预览" else "视频预览",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            if (item.type == PendingMediaType.IMAGE) {
                ImagePreviewContent(uri = item.uri)
            } else {
                VideoPreviewContent(uri = item.uri)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ImagePreviewContent(uri: Uri) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            ImageView(it).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                setImageURI(uri)
            }
        },
        update = { imageView ->
            imageView.setImageURI(uri)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    )
}

@Composable
private fun VideoPreviewContent(uri: Uri) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            VideoView(it).apply {
                setVideoURI(uri)
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    start()
                }
            }
        },
        update = { videoView ->
            videoView.setVideoURI(uri)
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                videoView.start()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    )
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
            "接入会话、查看 AI 步骤、发送标注",
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

        /* SoftCard(modifier = Modifier.fillMaxWidth()) {
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
        }*/
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadEntrySheet(
    onDismiss: () -> Unit,
    onUploadClick: () -> Unit,
    onCaptureClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "选择操作",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            SheetActionItem(
                icon = Icons.Filled.UploadFile,
                title = "上传",
                subtitle = "从本地选择照片或视频",
                onClick = onUploadClick
            )

            SheetActionItem(
                icon = Icons.Filled.PhotoCamera,
                title = "拍摄",
                subtitle = "使用相机拍摄照片或视频",
                onClick = onCaptureClick
            )

            Spacer(Modifier.height(8.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadTypeSheet(
    title: String,
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            SheetActionItem(
                icon = Icons.Filled.Image,
                title = "照片",
                subtitle = "选择图片文件",
                onClick = onImageClick
            )

            SheetActionItem(
                icon = Icons.Filled.Videocam,
                title = "视频",
                subtitle = "选择视频文件",
                onClick = onVideoClick
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContinueAddSheet(
    type: PendingMediaType,
    onDismiss: () -> Unit,
    onPickClick: () -> Unit,
    onCaptureClick: () -> Unit
) {
    val title = if (type == PendingMediaType.IMAGE) "继续添加照片" else "继续添加视频"
    val pickText = if (type == PendingMediaType.IMAGE) "从本地选择照片" else "从本地选择视频"
    val captureText = if (type == PendingMediaType.IMAGE) "继续拍摄照片" else "继续拍摄视频"
    val icon1 = if (type == PendingMediaType.IMAGE) Icons.Filled.UploadFile else Icons.Filled.UploadFile
    val icon2 = if (type == PendingMediaType.IMAGE) Icons.Filled.PhotoCamera else Icons.Filled.Videocam

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            SheetActionItem(
                icon = icon1,
                title = "上传",
                subtitle = pickText,
                onClick = onPickClick
            )

            SheetActionItem(
                icon = icon2,
                title = "拍摄",
                subtitle = captureText,
                onClick = onCaptureClick
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SheetActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pressClick(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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

private fun createImageUri(context: Context): Uri {
    val contentValues = ContentValues().apply {
        put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "merchant_image_${System.currentTimeMillis()}.jpg"
        )
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: throw IllegalStateException("无法创建图片 Uri")
}

private fun createVideoUri(context: Context): Uri {
    val contentValues = ContentValues().apply {
        put(
            MediaStore.Video.Media.DISPLAY_NAME,
            "merchant_video_${System.currentTimeMillis()}.mp4"
        )
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
    }

    return context.contentResolver.insert(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: throw IllegalStateException("无法创建视频 Uri")
}