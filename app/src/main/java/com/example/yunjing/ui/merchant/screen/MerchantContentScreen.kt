package com.example.yunjing.ui.merchant.screen

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import kotlinx.coroutines.delay
import com.example.yunjing.ui.merchant.util.createVideoUri
import com.example.yunjing.ui.merchant.util.createImageUri
import com.example.yunjing.ui.merchant.component.*
import com.example.yunjing.ui.merchant.model.*

private const val UNITY_PLAYER_PACKAGE = "com.example.yunjing.tutorialplayer"

@Composable
fun MerchantContentScreen() {
    val context = LocalContext.current

    var currentProject by remember {
        mutableStateOf(
            MerchantContentProject(
                id = "project_1",
                name = "项目1",
                status = "编辑中",
                summary = "支持上传、重建与解析"
            )
        )
    }

    var previewUploadItem by remember { mutableStateOf<PendingUploadItem?>(null) }
    var previewAssetItem by remember { mutableStateOf<MerchantAssetItem?>(null) }

    val recentItems = remember {
        listOf(
            ContentItem("螺旋风扇安装教程", "版本 v1.2 · 已发布", "已发布"),
            ContentItem("桌面支架拆装说明", "版本 v0.9 · 待优化", "待处理"),
            ContentItem("蓝牙音箱教学内容", "版本 v1.0 · 审核中", "审核中")
        )
    }

    var showEntrySheet by remember { mutableStateOf(false) }
    var showPickTypeSheet by remember { mutableStateOf(false) }
    var showCaptureTypeSheet by remember { mutableStateOf(false) }
    var showContinueAddSheet by remember { mutableStateOf(false) }

    var activeUploadType by rememberSaveable { mutableStateOf<PendingMediaType?>(null) }
    var isUploadSessionActive by rememberSaveable { mutableStateOf(false) }
    var pendingUploads by remember { mutableStateOf(listOf<PendingUploadItem>()) }

    val rebuildAssets = remember { mutableStateListOf<MerchantAssetItem>() }
    var rebuildProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var isRebuilding by rememberSaveable { mutableStateOf(false) }
    var rebuildResult by remember { mutableStateOf<RebuildResult?>(null) }

    val parseAssets = remember { mutableStateListOf<MerchantAssetItem>() }
    var selectedParseMode by rememberSaveable { mutableStateOf(ParseMode.EXPLODED_GUIDE) }
    var parseProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var isParsing by rememberSaveable { mutableStateOf(false) }
    var parseResult by remember { mutableStateOf<ParseResult?>(null) }

    fun resetUploadBatch() {
        pendingUploads = emptyList()
        activeUploadType = null
        isUploadSessionActive = false
        previewUploadItem = null
    }

    fun buildUploadName(type: PendingMediaType, index: Int): String {
        return if (type == PendingMediaType.IMAGE) "图片 ${index + 1}" else "视频 ${index + 1}"
    }

    fun buildAssetName(type: MerchantAssetType, index: Int): String {
        return when (type) {
            MerchantAssetType.IMAGE -> "图片 ${index + 1}"
            MerchantAssetType.VIDEO -> "视频 ${index + 1}"
            MerchantAssetType.MODEL -> "模型 ${index + 1}"
        }
    }

    fun canAddType(type: PendingMediaType): Boolean {
        return activeUploadType == null || activeUploadType == type
    }

    fun startUploadSession(type: PendingMediaType) {
        if (activeUploadType == null) {
            activeUploadType = type
        }
        isUploadSessionActive = true
    }

    fun appendUploads(uris: List<Uri>, type: PendingMediaType, source: String) {
        if (uris.isEmpty()) return
        val startIndex = pendingUploads.size
        pendingUploads = pendingUploads + uris.mapIndexed { index, uri ->
            PendingUploadItem(
                uri = uri,
                type = type,
                name = buildUploadName(type, startIndex + index),
                source = source
            )
        }
    }

    fun appendAssets(
        target: MutableList<MerchantAssetItem>,
        uris: List<Uri>,
        type: MerchantAssetType,
        source: String
    ) {
        if (uris.isEmpty()) return
        val startIndex = target.size
        uris.forEachIndexed { index, uri ->
            target.add(
                MerchantAssetItem(
                    uri = uri,
                    type = type,
                    name = buildAssetName(type, startIndex + index),
                    source = source
                )
            )
        }
    }

    fun normalizeAssets(target: MutableList<MerchantAssetItem>) {
        val rebuilt = target.mapIndexed { index, item ->
            item.copy(name = buildAssetName(item.type, index))
        }
        target.clear()
        target.addAll(rebuilt)
    }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendUploads(uris, PendingMediaType.IMAGE, "本地上传")
    }

    val pickVideosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendUploads(uris, PendingMediaType.VIDEO, "本地上传")
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            appendUploads(listOf(cameraImageUri!!), PendingMediaType.IMAGE, "拍摄导入")
        }
    }

    var cameraVideoUri by remember { mutableStateOf<Uri?>(null) }
    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && cameraVideoUri != null) {
            appendUploads(listOf(cameraVideoUri!!), PendingMediaType.VIDEO, "拍摄导入")
        }
    }

    fun launchTakePhoto() {
        val uri = createImageUri(context)
        cameraImageUri = uri
        takePhotoLauncher.launch(uri)
    }

    fun launchTakeVideo() {
        val uri = createVideoUri(context)
        cameraVideoUri = uri
        takeVideoLauncher.launch(uri)
    }

    val pickRebuildImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendAssets(rebuildAssets, uris, MerchantAssetType.IMAGE, "重建图片")
    }

    val pickParseImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendAssets(parseAssets, uris, MerchantAssetType.IMAGE, "解析图片")
    }

    val pickParseVideosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendAssets(parseAssets, uris, MerchantAssetType.VIDEO, "解析视频")
    }

    val openModelsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val modelUris = uris.filter { uri ->
            val value = uri.toString().lowercase()
            value.endsWith(".glb") || value.contains(".glb") || value.contains("model")
        }
        if (modelUris.isEmpty() && uris.isNotEmpty()) {
            Toast.makeText(context, "当前仅建议选择 .glb 模型文件", Toast.LENGTH_SHORT).show()
        }
        appendAssets(parseAssets, modelUris, MerchantAssetType.MODEL, "模型文件")
    }

    LaunchedEffect(isRebuilding) {
        if (isRebuilding) {
            rebuildProgress = 0f
            repeat(20) {
                delay(140)
                rebuildProgress = (it + 1) / 20f
            }
            isRebuilding = false
            rebuildResult = RebuildResult(
                modelName = "${currentProject.name} · 重建模型",
                modelAssetName = "preset_${currentProject.id}.glb",
                coverText = "预置模型",
                statusText = "已完成 fake 3D 重建，可进入模型浏览占位流程查看预置 .glb 文件。"
            )
            Toast.makeText(context, "重建完成", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isParsing, selectedParseMode) {
        if (isParsing) {
            parseProgress = 0f
            repeat(24) {
                delay(120)
                parseProgress = (it + 1) / 24f
            }
            isParsing = false
            parseResult = if (selectedParseMode == ParseMode.EXPLODED_GUIDE) {
                ParseResult(
                    mode = ParseMode.EXPLODED_GUIDE,
                    explodedImageName = "exploded_${currentProject.id}.png",
                    statusText = "已完成 fake 解析，当前展示的是爆炸图说明书占位结果，后续可替换为真实图像。"
                )
            } else {
                ParseResult(
                    mode = ParseMode.VIDEO_GUIDE,
                    tutorialVideoName = "tutorial_${currentProject.id}.mp4",
                    statusText = "已完成 fake 解析，当前可跳转到 Unity 教程播放器占位应用查看预置安装视频。"
                )
            }
            Toast.makeText(context, "解析完成", Toast.LENGTH_SHORT).show()
        }
    }

    fun openUnityPlayer() {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(UNITY_PLAYER_PACKAGE)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(
                    context,
                    "Unity 播放器占位包未接入，当前包名：$UNITY_PLAYER_PACKAGE",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "未找到教程播放器应用", Toast.LENGTH_SHORT).show()
        }
    }

    fun browseModelResult() {
        previewAssetItem = MerchantAssetItem(
            uri = Uri.EMPTY,
            type = MerchantAssetType.MODEL,
            name = rebuildResult?.modelAssetName ?: "preset_model.glb",
            source = "预置模型浏览"
        )
    }

    fun createNextProject() {
        val nextIndex = (currentProject.id.substringAfterLast("_").toIntOrNull() ?: 1) + 1
        currentProject = MerchantContentProject(
            id = "project_$nextIndex",
            name = "项目$nextIndex",
            status = "编辑中",
            summary = "新建后可选择上传 / 重建 / 解析"
        )
        resetUploadBatch()
        rebuildAssets.clear()
        parseAssets.clear()
        rebuildResult = null
        parseResult = null
        rebuildProgress = 0f
        parseProgress = 0f
        Toast.makeText(context, "已新建 ${currentProject.name}", Toast.LENGTH_SHORT).show()
    }

    val canPublish = pendingUploads.isNotEmpty() || rebuildResult != null || parseResult != null

    val hasParseModel = parseAssets.any { it.type == MerchantAssetType.MODEL }
    val hasParseSource = if (selectedParseMode == ParseMode.EXPLODED_GUIDE) {
        parseAssets.any { it.type == MerchantAssetType.IMAGE }
    } else {
        parseAssets.any { it.type == MerchantAssetType.VIDEO }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(Modifier.height(14.dp))
            Text("内容库", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "以项目为单位管理上传、重建、解析与发布流程",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            ProjectHeaderCard(
                project = currentProject,
                onNewProject = { createNextProject() },
                onPublish = {
                    if (!canPublish) {
                        Toast.makeText(context, "请先产出可检查的内容后再发布", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "发布成功（当前为本地占位逻辑，后续可绑定二维码）", Toast.LENGTH_SHORT).show()
                    }
                },
                canPublish = canPublish
            )

            Spacer(Modifier.height(14.dp))

            WorkbenchSectionCard(
                title = "上传",
                desc = "保持原有上传逻辑，可多次上传或拍摄照片、视频；当前仅调整内容库承载方式。",
                actionText = if (isUploadSessionActive) "继续上传" else "开始上传",
                icon = Icons.Filled.UploadFile,
                onAction = {
                    if (pendingUploads.isNotEmpty() && !isUploadSessionActive) {
                        Toast.makeText(
                            context,
                            "当前批次已结束，请先解析或清空后再开始新批次",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showEntrySheet = true
                    }
                }
            ) {
                if (pendingUploads.isEmpty()) {
                    Text(
                        "当前项目暂未上传内容。",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = buildString {
                            append("当前批次：")
                            append(if (activeUploadType == PendingMediaType.IMAGE) "照片" else "视频")
                            append(" · 共 ${pendingUploads.size} 项")
                            append(if (isUploadSessionActive) " · 上传中" else " · 待后续处理")
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
                                    Toast.makeText(context, "当前批次已结束", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniChip(
                            text = "清空上传",
                            icon = Icons.Filled.ClearAll,
                            onClick = {
                                resetUploadBatch()
                                Toast.makeText(context, "上传批次已清空", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pendingUploads.forEachIndexed { index, item ->
                            PendingUploadRow(
                                item = item,
                                onPreview = { previewUploadItem = item },
                                onRemove = {
                                    val mutable = pendingUploads.toMutableList()
                                    mutable.removeAt(index)
                                    pendingUploads = mutable.mapIndexed { newIndex, old ->
                                        old.copy(name = buildUploadName(old.type, newIndex))
                                    }
                                    if (pendingUploads.isEmpty()) {
                                        activeUploadType = null
                                        isUploadSessionActive = false
                                        previewUploadItem = null
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            WorkbenchSectionCard(
                title = "重建",
                desc = "仅允许选择照片，支持删除图片并进行 fake 3D 重建；完成后可浏览预置 .glb 模型。",
                actionText = "添加重建图片",
                icon = Icons.Filled.ViewInAr,
                onAction = {
                    pickRebuildImagesLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                if (rebuildAssets.isEmpty()) {
                    Text(
                        "当前尚未添加重建图片。",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        rebuildAssets.forEachIndexed { index, item ->
                            AssetRow(
                                item = item,
                                onPreview = {
                                    Toast.makeText(context, "当前先复用图片选择结果展示", Toast.LENGTH_SHORT).show()
                                },
                                onRemove = {
                                    rebuildAssets.removeAt(index)
                                    normalizeAssets(rebuildAssets)
                                    if (rebuildAssets.isEmpty()) rebuildResult = null
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniChip(
                            text = "清空图片",
                            icon = Icons.Filled.ClearAll,
                            onClick = {
                                rebuildAssets.clear()
                                rebuildResult = null
                                rebuildProgress = 0f
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MiniChip(
                            text = "开始重建",
                            icon = Icons.Filled.ViewInAr,
                            onClick = {
                                if (rebuildAssets.isEmpty()) {
                                    Toast.makeText(context, "请先添加图片", Toast.LENGTH_SHORT).show()
                                } else if (!isRebuilding) {
                                    rebuildResult = null
                                    isRebuilding = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isRebuilding) {
                    Spacer(Modifier.height(12.dp))
                    ProgressBlock(
                        title = "3D 重建中",
                        progress = rebuildProgress,
                        hint = "当前为 fake 进度条，后续可接真实重建后端"
                    )
                }

                if (rebuildResult != null) {
                    Spacer(Modifier.height(12.dp))
                    RebuildResultCard(
                        modelName = rebuildResult!!.modelName,
                        statusText = rebuildResult!!.statusText,
                        onBrowseModel = { browseModelResult() }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            WorkbenchSectionCard(
                title = "解析",
                desc = "区分说明书照片 + 模型生成爆炸图，或视频 + 模型生成可视化教程两种流程。",
                actionText = if (selectedParseMode == ParseMode.EXPLODED_GUIDE) "切到视频教程解析" else "切到爆炸图解析",
                icon = Icons.Filled.Description,
                onAction = {
                    selectedParseMode = if (selectedParseMode == ParseMode.EXPLODED_GUIDE) {
                        ParseMode.VIDEO_GUIDE
                    } else {
                        ParseMode.EXPLODED_GUIDE
                    }
                    parseAssets.clear()
                    parseResult = null
                    parseProgress = 0f
                }
            ) {
                Text(
                    text = if (selectedParseMode == ParseMode.EXPLODED_GUIDE)
                        "当前模式：说明书照片 + 模型 → 爆炸图"
                    else
                        "当前模式：视频 + 模型 → 可视化教程",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedParseMode == ParseMode.EXPLODED_GUIDE) {
                        MiniChip(
                            text = "选择图片",
                            icon = Icons.Filled.Image,
                            onClick = {
                                pickParseImagesLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        MiniChip(
                            text = "选择视频",
                            icon = Icons.Filled.Videocam,
                            onClick = {
                                pickParseVideosLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MiniChip(
                        text = "选择模型",
                        icon = Icons.Filled.ViewInAr,
                        onClick = {
                            openModelsLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (parseAssets.isEmpty()) {
                    Text(
                        "当前尚未添加解析文件。",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        parseAssets.forEachIndexed { index, item ->
                            AssetRow(
                                item = item,
                                onPreview = { previewAssetItem = item },
                                onRemove = {
                                    parseAssets.removeAt(index)
                                    normalizeAssets(parseAssets)
                                    if (parseAssets.isEmpty()) {
                                        parseResult = null
                                        parseProgress = 0f
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniChip(
                            text = "清空解析文件",
                            icon = Icons.Filled.ClearAll,
                            onClick = {
                                parseAssets.clear()
                                parseResult = null
                                parseProgress = 0f
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MiniChip(
                            text = "开始解析",
                            icon = Icons.Filled.Description,
                            onClick = {
                                if (!hasParseModel) {
                                    Toast.makeText(context, "请至少添加一个模型文件", Toast.LENGTH_SHORT).show()
                                } else if (!hasParseSource) {
                                    Toast.makeText(
                                        context,
                                        if (selectedParseMode == ParseMode.EXPLODED_GUIDE) "请添加说明书图片"
                                        else "请添加教程视频",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (!isParsing) {
                                    parseResult = null
                                    isParsing = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isParsing) {
                    Spacer(Modifier.height(12.dp))
                    ProgressBlock(
                        title = "内容解析中",
                        progress = parseProgress,
                        hint = "当前为 fake 解析进度，后续可接真实生成服务"
                    )
                }

                if (parseResult != null) {
                    Spacer(Modifier.height(12.dp))
                    ParseResultCard(
                        mode = parseResult!!.mode,
                        statusText = parseResult!!.statusText,
                        onOpenVideo = { openUnityPlayer() }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("最近内容", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
        }

        items(recentItems) { item ->
            ContentRow(
                title = item.title,
                subtitle = item.subtitle,
                badge = item.badge,
                onClick = {}
            )
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

    if (previewUploadItem != null) {
        MediaPreviewSheet(
            item = previewUploadItem!!,
            onDismiss = { previewUploadItem = null }
        )
    }

    LaunchedEffect(previewAssetItem) {
        previewAssetItem?.let {
            Toast.makeText(
                context,
                "当前为占位预览：${it.name}",
                Toast.LENGTH_SHORT
            ).show()
            previewAssetItem = null
        }
    }
}