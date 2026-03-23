package com.example.yunjing.ui.merchant.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yunjing.ui.merchant.component.*
import com.example.yunjing.ui.merchant.model.ContentItem
import com.example.yunjing.ui.merchant.model.PendingMediaType
import com.example.yunjing.ui.merchant.model.PendingUploadItem
import com.example.yunjing.ui.merchant.util.createImageUri
import com.example.yunjing.ui.merchant.util.createVideoUri

@Composable
fun MerchantContentScreen() {
    val context = LocalContext.current

    val items = remember {
        listOf(
            ContentItem("智能门锁 · 安装教程", "版本 v1.2 · 2 天前更新", "已发布"),
            ContentItem("书架 · 说明书 PDF", "版本 v1.0 · 刚上传", "待解析"),
            ContentItem("小家电 · 3D 模型", "版本 v0.9 · 待校验", "待审核")
        )
    }

    var showContinueAddSheet by remember { mutableStateOf(false) }
    var showEntrySheet by remember { mutableStateOf(false) }
    var showPickTypeSheet by remember { mutableStateOf(false) }
    var showCaptureTypeSheet by remember { mutableStateOf(false) }

    var previewItem by remember { mutableStateOf<PendingUploadItem?>(null) }

    var pendingUploads by remember { mutableStateOf(listOf<PendingUploadItem>()) }
    var activeUploadType by remember { mutableStateOf<PendingMediaType?>(null) }
    var isUploadSessionActive by remember { mutableStateOf(false) }

    var currentCaptureImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentCaptureVideoUri by remember { mutableStateOf<Uri?>(null) }
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

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendPickedItems(uris, PendingMediaType.IMAGE, "本地选择")
    }

    val pickVideosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendPickedItems(uris, PendingMediaType.VIDEO, "本地选择")
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCaptureImageUri != null) {
            appendPickedItems(listOf(currentCaptureImageUri!!), PendingMediaType.IMAGE, "拍摄")
        }
    }

    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && currentCaptureVideoUri != null) {
            appendPickedItems(listOf(currentCaptureVideoUri!!), PendingMediaType.VIDEO, "拍摄")
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
            "管理说明书/模型/教程版本与发布状态",
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
                        icon = if (activeUploadType == PendingMediaType.IMAGE) Icons.Filled.Image else Icons.Filled.Videocam,
                        onClick = { showContinueAddSheet = true },
                        modifier = Modifier.weight(1f)
                    )

                    MiniChip(
                        text = "结束上传",
                        icon = Icons.Filled.CheckCircle,
                        onClick = {
                            isUploadSessionActive = false
                            showContinueAddSheet = false
                            Toast.makeText(context, "当前批次已结束，请进行解析", Toast.LENGTH_SHORT).show()
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
                        onClick = {}
                    )
                }
            }
        }
    }

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

    if (previewItem != null) {
        MediaPreviewSheet(
            item = previewItem!!,
            onDismiss = { previewItem = null }
        )
    }
}