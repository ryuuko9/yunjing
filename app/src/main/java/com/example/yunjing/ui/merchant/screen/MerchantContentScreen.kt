package com.example.yunjing.ui.merchant.screen

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yunjing.ui.merchant.component.ContentRow
import com.example.yunjing.ui.merchant.component.CreateProjectDialog
import com.example.yunjing.ui.merchant.component.MaterialFolderCard
import com.example.yunjing.ui.merchant.component.MediaPreviewSheet
import com.example.yunjing.ui.merchant.component.MiniChip
import com.example.yunjing.ui.merchant.component.ModelFileRow
import com.example.yunjing.ui.merchant.component.PendingUploadRow
import com.example.yunjing.ui.merchant.component.PrimaryPillButton
import com.example.yunjing.ui.merchant.component.ProgressBlock
import com.example.yunjing.ui.merchant.component.ProjectListItem
import com.example.yunjing.ui.merchant.component.RebuildResultCard
import com.example.yunjing.ui.merchant.component.RenameProjectDialog
import com.example.yunjing.ui.merchant.component.SelectableUploadRow
import com.example.yunjing.ui.merchant.component.SoftCard
import com.example.yunjing.ui.merchant.component.UploadEntrySheet
import com.example.yunjing.ui.merchant.component.WorkbenchSectionCard
import com.example.yunjing.ui.merchant.model.ContentItem
import com.example.yunjing.ui.merchant.model.ContentPageState
import com.example.yunjing.ui.merchant.model.MerchantContentProject
import com.example.yunjing.ui.merchant.model.ParseMode
import com.example.yunjing.ui.merchant.model.ParseResult
import com.example.yunjing.ui.merchant.model.PendingMediaType
import com.example.yunjing.ui.merchant.model.PendingUploadItem
import com.example.yunjing.ui.merchant.model.ProjectModelItem
import com.example.yunjing.ui.merchant.model.RebuildResult
import com.example.yunjing.ui.merchant.util.createImageUri
import kotlinx.coroutines.delay
import com.example.yunjing.ui.merchant.component.ProjectSettingMenu
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yunjing.ui.merchant.viewmodel.MerchantContentViewModel

private const val UNITY_PLAYER_PACKAGE = "com.example.yunjing.tutorialplayer"

@Composable
fun MerchantContentScreen(
    viewModel: MerchantContentViewModel
) {
    val context = LocalContext.current
    val projectDetailListState = rememberLazyListState()
    val mediaManageListState = rememberLazyListState()
    val rebuildSelectListState = rememberLazyListState()
    val parseSelectListState = rememberLazyListState()

    val projects = viewModel.projects
    val selectedProjectId = viewModel.selectedProjectId
    val pageState = viewModel.pageState
    val showCreateProjectDialog = viewModel.showCreateProjectDialog
    val showRenameProjectDialog = viewModel.showRenameProjectDialog
    val showDeleteProjectDialog = viewModel.showDeleteProjectDialog
    val showEntrySheet = viewModel.showEntrySheet
    val previewItem = viewModel.previewItem
    val currentCaptureImageUri = viewModel.currentCaptureImageUri
    val pendingCameraAction = viewModel.pendingCameraAction
    val currentProject = viewModel.currentProject()
    val recentItems = viewModel.recentItems

    fun normalizeUploads(list: List<PendingUploadItem>): List<PendingUploadItem> {
        var imageCount = 0
        var videoCount = 0
        return list.map { item ->
            when (item.type) {
                PendingMediaType.IMAGE -> {
                    imageCount += 1
                    item.copy(name = "图片 $imageCount")
                }
                PendingMediaType.VIDEO -> {
                    videoCount += 1
                    item.copy(name = "视频 $videoCount")
                }
            }
        }
    }

    fun detectMediaType(uri: Uri): PendingMediaType {
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        return if (mimeType.startsWith("video")) PendingMediaType.VIDEO else PendingMediaType.IMAGE
    }

    fun appendUploadsToCurrentProject(uris: List<Uri>, source: String) {
        val project = currentProject ?: return
        if (uris.isEmpty()) return

        val imageCountBase = project.uploads.count { it.type == PendingMediaType.IMAGE }
        val videoCountBase = project.uploads.count { it.type == PendingMediaType.VIDEO }

        var imageIndex = imageCountBase
        var videoIndex = videoCountBase

        val newItems = uris.map { uri ->
            val type = detectMediaType(uri)
            val name = when (type) {
                PendingMediaType.IMAGE -> {
                    imageIndex += 1
                    "图片 $imageIndex"
                }
                PendingMediaType.VIDEO -> {
                    videoIndex += 1
                    "视频 $videoIndex"
                }
            }

            PendingUploadItem(
                uri = uri,
                type = type,
                name = name,
                source = source
            )
        }

        viewModel.updateProject(project.id) {
            val merged = it.uploads + newItems
            it.copy(
                uploads = merged,
                summary = "已上传 ${merged.size} 项素材"
            )
        }
    }

    fun removeUploadFromCurrentProject(item: PendingUploadItem) {
        val project = currentProject ?: return
        viewModel.updateProject(project.id) {
            val remainingUploads = normalizeUploads(it.uploads.filterNot { upload -> upload.uri == item.uri })
            val validUploadUris = remainingUploads.map { upload -> upload.uri.toString() }.toSet()

            it.copy(
                uploads = remainingUploads,
                selectedRebuildUris = it.selectedRebuildUris.filter { uri -> uri in validUploadUris },
                selectedParseSourceUris = it.selectedParseSourceUris.filter { uri -> uri in validUploadUris },
                summary = if (remainingUploads.isEmpty()) "待上传内容" else "已上传 ${remainingUploads.size} 项素材"
            )
        }
    }

    fun clearUploadsOfCurrentProject() {
        val project = currentProject ?: return
        viewModel.updateProject(project.id) {
            it.copy(
                uploads = emptyList(),
                selectedRebuildUris = emptyList(),
                selectedParseSourceUris = emptyList(),
                rebuildResult = null,
                explodedGuideResult = null,
                videoGuideResult = null,
                rebuildProgress = 0f,
                parseProgress = 0f,
                summary = "待上传内容"
            )
        }
    }

    fun toggleRebuildMedia(uri: Uri) {
        val project = currentProject ?: return
        val key = uri.toString()
        val current = project.selectedRebuildUris.toMutableList()
        if (current.contains(key)) current.remove(key) else current.add(key)

        viewModel.updateProject(project.id) {
            it.copy(selectedRebuildUris = current)
        }
    }

    fun toggleParseSource(uri: Uri) {
        val project = currentProject ?: return
        val key = uri.toString()
        val current = project.selectedParseSourceUris.toMutableList()
        if (current.contains(key)) current.remove(key) else current.add(key)

        viewModel.updateProject(project.id) {
            it.copy(selectedParseSourceUris = current)
        }
    }

    fun toggleParseModel(uri: Uri) {
        val project = currentProject ?: return
        val key = uri.toString()
        val current = project.selectedParseModelUris.toMutableList()
        if (current.contains(key)) current.remove(key) else current.add(key)

        viewModel.updateProject(project.id) {
            it.copy(selectedParseModelUris = current)
        }
    }

    fun removeParseModel(uri: Uri) {
        val project = currentProject ?: return
        val key = uri.toString()
        viewModel.updateProject(project.id) {
            it.copy(
                parseModels = it.parseModels.filterNot { model -> model.uri == uri },
                selectedParseModelUris = it.selectedParseModelUris.filterNot { selected -> selected == key }
            )
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingCameraAction?.invoke()
            viewModel.pendingCameraAction = null
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
            viewModel.pendingCameraAction = onGranted
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        appendUploadsToCurrentProject(uris, "本地选择")
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCaptureImageUri != null) {
            appendUploadsToCurrentProject(listOf(currentCaptureImageUri!!), "拍摄")
        }
    }

    val openModelsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val project = currentProject ?: return@rememberLauncherForActivityResult
        val validModels = uris.filter { uri ->
            val value = uri.toString().lowercase()
            value.endsWith(".glb") || value.contains(".glb") || value.contains("model")
        }

        if (validModels.isEmpty() && uris.isNotEmpty()) {
            Toast.makeText(context, "当前仅建议选择 .glb 模型文件", Toast.LENGTH_SHORT).show()
        }

        viewModel.updateProject(project.id) {
            val append = validModels.mapIndexed { index, uri ->
                val name = "模型 ${it.parseModels.size + index + 1}.glb"
                ProjectModelItem(uri = uri, name = name)
            }
            it.copy(parseModels = it.parseModels + append)
        }
    }

    fun launchTakePhoto() {
        ensureCameraPermission {
            val uri = createImageUri(context)
            viewModel.currentCaptureImageUri = uri
            takePhotoLauncher.launch(uri)
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

    LaunchedEffect(currentProject?.id, currentProject?.isRebuilding) {
        val project = currentProject ?: return@LaunchedEffect
        if (project.isRebuilding) {
            repeat(20) { index ->
                delay(140)
                viewModel.updateProject(project.id) {
                    it.copy(rebuildProgress = (index + 1) / 20f)
                }
            }
            viewModel.updateProject(project.id) {
                it.copy(
                    isRebuilding = false,
                    rebuildProgress = 1f,
                    rebuildResult = RebuildResult(
                        modelName = "${it.name} · 重建模型",
                        statusText = "已完成 fake 3D 重建，可进入预置 .glb 模型浏览流程。"
                    ),
                    status = "待解析",
                    summary = "重建结果已生成"
                )
            }
            Toast.makeText(context, "重建完成", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(currentProject?.id, currentProject?.isParsing) {
        val project = currentProject ?: return@LaunchedEffect
        if (project.isParsing) {
            repeat(24) { index ->
                delay(120)
                viewModel.updateProject(project.id) {
                    it.copy(parseProgress = (index + 1) / 24f)
                }
            }

            viewModel.updateProject(project.id) {
                val result = ParseResult(
                    mode = it.parseMode,
                    statusText = if (it.parseMode == ParseMode.EXPLODED_GUIDE) {
                        "已完成 fake 解析，爆炸图说明书结果已生成，可重复查看。"
                    } else {
                        "已完成 fake 解析，教程播放器入口已生成，可重复打开。"
                    }
                )

                if (it.parseMode == ParseMode.EXPLODED_GUIDE) {
                    it.copy(
                        isParsing = false,
                        parseProgress = 1f,
                        explodedGuideResult = result,
                        status = "待发布",
                        summary = "解析结果已生成"
                    )
                } else {
                    it.copy(
                        isParsing = false,
                        parseProgress = 1f,
                        videoGuideResult = result,
                        status = "待发布",
                        summary = "解析结果已生成"
                    )
                }
            }

            Toast.makeText(context, "解析完成", Toast.LENGTH_SHORT).show()
        }
    }

    when (pageState) {
        ContentPageState.PROJECT_LIST -> {
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

                    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("项目管理", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(10.dp))
                            MiniChip(
                                text = "新建项目",
                                icon = Icons.Filled.Add,
                                onClick = { viewModel.showCreateProjectDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("项目列表", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                }

                items(projects) { project ->
                    ProjectListItem(
                        project = project,
                        onClick = {
                            viewModel.selectedProjectId = project.id
                            viewModel.pageState = ContentPageState.PROJECT_DETAIL
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    if (recentItems.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("最近内容", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        SoftCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                recentItems.forEach { item ->
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
                }
            }
        }

        ContentPageState.PROJECT_DETAIL -> {
            if (currentProject == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                val canPublish = currentProject.rebuildResult != null ||
                        currentProject.explodedGuideResult != null ||
                        currentProject.videoGuideResult != null

                LazyColumn(
                    state = projectDetailListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MiniChip(
                                text = "返回项目列表",
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                onClick = {
                                    viewModel.selectedProjectId = null
                                    viewModel.pageState = ContentPageState.PROJECT_LIST
                                },
                                modifier = Modifier.weight(1f)
                            )

                            ProjectSettingMenu(
                                modifier = Modifier.weight(1f),
                                onRename = {
                                    viewModel.showRenameProjectDialog = true
                                },
                                onDelete = {
                                    viewModel.showDeleteProjectDialog = true
                                }
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(currentProject.name, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${currentProject.summary} · ${currentProject.status}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(18.dp))

                        WorkbenchSectionCard(
                            title = "素材管理",
                            desc = "当前项目的图片与视频素材统一存放在素材文件夹中，支持继续添加、预览与删除。",
                            actionArea = {}
                        ) {
                            MaterialFolderCard(
                                mediaCount = currentProject.uploads.size,
                                onClick = {
                                    viewModel.pageState = ContentPageState.MEDIA_FOLDER_MANAGE
                                }
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        WorkbenchSectionCard(
                            title = "重建",
                            desc = "从当前项目素材文件夹中进入选择模式，勾选图片或视频素材后发起重建。",
                            actionArea = {}
                        ) {
                            MiniChip(
                                text = "进入素材文件夹选择重建图片",
                                icon = Icons.Filled.ViewInAr,
                                onClick = {
                                    if (currentProject.uploads.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "当前项目暂无素材，请先进入素材文件夹添加内容",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        viewModel.pageState = ContentPageState.MEDIA_FOLDER_SELECT_REBUILD
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (currentProject.selectedRebuildUris.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "已选择 ${currentProject.selectedRebuildUris.size} 项重建素材",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    MiniChip(
                                        text = "清空选择",
                                        icon = Icons.Filled.ClearAll,
                                        onClick = {
                                            viewModel.updateProject(currentProject.id) {
                                                it.copy(
                                                    selectedRebuildUris = emptyList(),
                                                    rebuildResult = null,
                                                    rebuildProgress = 0f
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    MiniChip(
                                        text = "开始重建",
                                        icon = Icons.Filled.ViewInAr,
                                        onClick = {
                                            val selectedItems = currentProject.uploads.filter { upload ->
                                                currentProject.selectedRebuildUris.contains(upload.uri.toString())
                                            }

                                            if (selectedItems.isEmpty()) {
                                                Toast.makeText(context, "请先选择图片素材后再开始重建", Toast.LENGTH_SHORT).show()
                                            } else if (selectedItems.any { it.type != PendingMediaType.IMAGE }) {
                                                Toast.makeText(context, "重建仅支持图片素材，请重新选择", Toast.LENGTH_SHORT).show()
                                            } else if (!currentProject.isRebuilding) {
                                                viewModel.updateProject(currentProject.id) {
                                                    it.copy(
                                                        isRebuilding = true,
                                                        rebuildProgress = 0f,
                                                        rebuildResult = null
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            if (currentProject.isRebuilding) {
                                Spacer(Modifier.height(12.dp))
                                ProgressBlock(
                                    title = "3D 重建中",
                                    progress = currentProject.rebuildProgress,
                                    hint = "当前为 fake 进度条，后续可接真实重建后端"
                                )
                            }

                            if (currentProject.rebuildResult != null) {
                                Spacer(Modifier.height(12.dp))
                                RebuildResultCard(
                                    modelName = currentProject.rebuildResult.modelName,
                                    statusText = currentProject.rebuildResult.statusText,
                                    onBrowseModel = {
                                        Toast.makeText(
                                            context,
                                            "此处进入预置 .glb 模型浏览功能（占位）",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        WorkbenchSectionCard(
                            title = "解析",
                            desc = "解析素材从当前项目素材文件夹中进入选择模式，模型文件继续单独导入。",
                            actionArea = {
                                MiniChip(
                                    text = if (currentProject.parseMode == ParseMode.EXPLODED_GUIDE) {
                                        "切到视频教程解析"
                                    } else {
                                        "切到爆炸图解析"
                                    },
                                    icon = Icons.Filled.Description,
                                    onClick = {
                                        viewModel.updateProject(currentProject.id) {
                                            it.copy(
                                                parseMode = if (it.parseMode == ParseMode.EXPLODED_GUIDE) {
                                                    ParseMode.VIDEO_GUIDE
                                                } else {
                                                    ParseMode.EXPLODED_GUIDE
                                                },
                                                selectedParseSourceUris = emptyList(),
                                                parseProgress = 0f
                                            )
                                        }
                                    }
                                )
                            }
                        ) {
                            Text(
                                text = if (currentProject.parseMode == ParseMode.EXPLODED_GUIDE) {
                                    "当前模式：说明书照片 + 模型 → 爆炸图"
                                } else {
                                    "当前模式：视频 + 模型 → 可视化教程"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(12.dp))

                            MiniChip(
                                text = if (currentProject.parseMode == ParseMode.EXPLODED_GUIDE) {
                                    "进入素材文件夹选择说明书图片"
                                } else {
                                    "进入素材文件夹选择解析视频"
                                },
                                icon = Icons.Filled.Description,
                                onClick = {
                                    if (currentProject.uploads.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "当前项目暂无素材，请先进入素材文件夹添加内容",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        viewModel.pageState = ContentPageState.MEDIA_FOLDER_SELECT_PARSE
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MiniChip(
                                    text = "导入模型",
                                    icon = Icons.Filled.ViewInAr,
                                    onClick = { openModelsLauncher.launch(arrayOf("*/*")) },
                                    modifier = Modifier.weight(1f)
                                )
                                MiniChip(
                                    text = "清空解析选择",
                                    icon = Icons.Filled.ClearAll,
                                    onClick = {
                                        viewModel.updateProject(currentProject.id) {
                                            it.copy(
                                                selectedParseSourceUris = emptyList(),
                                                selectedParseModelUris = emptyList(),
                                                parseProgress = 0f
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (currentProject.selectedParseSourceUris.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "已选择 ${currentProject.selectedParseSourceUris.size} 项解析素材",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (currentProject.parseModels.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "已导入模型：",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    currentProject.parseModels.forEach { model ->
                                        ModelFileRow(
                                            item = model,
                                            selected = currentProject.selectedParseModelUris.contains(model.uri.toString()),
                                            onToggle = { toggleParseModel(model.uri) },
                                            onRemove = { removeParseModel(model.uri) }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            PrimaryPillButton(
                                text = "开始解析",
                                onClick = {
                                    val selectedParseItems = currentProject.uploads.filter { upload ->
                                        currentProject.selectedParseSourceUris.contains(upload.uri.toString())
                                    }

                                    if (selectedParseItems.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            if (currentProject.parseMode == ParseMode.EXPLODED_GUIDE) "请先选择说明书图片" else "请先选择解析视频",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (
                                        currentProject.parseMode == ParseMode.EXPLODED_GUIDE &&
                                        selectedParseItems.any { it.type != PendingMediaType.IMAGE }
                                    ) {
                                        Toast.makeText(context, "当前模式仅支持图片素材，请重新选择", Toast.LENGTH_SHORT).show()
                                    } else if (
                                        currentProject.parseMode == ParseMode.VIDEO_GUIDE &&
                                        selectedParseItems.any { it.type != PendingMediaType.VIDEO }
                                    ) {
                                        Toast.makeText(context, "当前模式仅支持视频素材，请重新选择", Toast.LENGTH_SHORT).show()
                                    } else if (currentProject.selectedParseModelUris.isEmpty()) {
                                        Toast.makeText(context, "请至少选择一个模型文件", Toast.LENGTH_SHORT).show()
                                    } else if (!currentProject.isParsing) {
                                        viewModel.updateProject(currentProject.id) {
                                            it.copy(
                                                isParsing = true,
                                                parseProgress = 0f,
                                            )
                                        }
                                    }
                                },
                            )

                            if (currentProject.isParsing) {
                                Spacer(Modifier.height(12.dp))
                                ProgressBlock(
                                    title = "内容解析中",
                                    progress = currentProject.parseProgress,
                                    hint = "当前为 fake 解析进度，后续可接真实生成服务"
                                )
                            }

                            if (currentProject.explodedGuideResult != null) {
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("爆炸图结果", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            currentProject.explodedGuideResult.statusText,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        PrimaryPillButton(
                                            text = "查看爆炸图",
                                            onClick = {
                                                Toast.makeText(context, "此处打开已生成的爆炸图说明书（占位）", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }

                            if (currentProject.videoGuideResult != null) {
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("教程播放器", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            currentProject.videoGuideResult.statusText,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        PrimaryPillButton(
                                            text = "打开教程播放器",
                                            onClick = { openUnityPlayer() }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PrimaryPillButton(
                                text = if (canPublish) "发布当前项目" else "发布当前项目（需先生成结果）",
                                onClick = {
                                    if (!canPublish) {
                                        Toast.makeText(context, "请先完成重建或解析中的至少一项成果", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateProject(currentProject.id) {
                                            it.copy(
                                                status = "已发布",
                                                summary = "版本 v1.0"
                                            )
                                        }
                                        Toast.makeText(context, "发布成功（当前为本地占位逻辑）", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        ContentPageState.MEDIA_FOLDER_MANAGE -> {
            if (currentProject == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                LazyColumn(
                    state = mediaManageListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MiniChip(
                                text = "返回项目",
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                onClick = { viewModel.pageState = ContentPageState.PROJECT_DETAIL },
                                modifier = Modifier.weight(1f)
                            )
                            MiniChip(
                                text = "添加素材",
                                icon = Icons.Filled.Add,
                                onClick = { viewModel.showEntrySheet = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Text("素材文件夹", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "统一管理当前项目内的图片与视频素材",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(18.dp))

                        if (currentProject.uploads.isNotEmpty()) {
                            PrimaryPillButton(
                                text = "清空当前素材",
                                onClick = {
                                    clearUploadsOfCurrentProject()
                                    Toast.makeText(context, "当前项目素材已清空", Toast.LENGTH_SHORT).show()
                                },
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    if (currentProject.uploads.isEmpty()) {
                        item {
                            Text(
                                "当前项目暂无素材，请点击“添加素材”继续上传。",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(currentProject.uploads) { item ->
                            PendingUploadRow(
                                item = item,
                                onPreview = { viewModel.previewItem = item },
                                onRemove = { removeUploadFromCurrentProject(item) }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        ContentPageState.MEDIA_FOLDER_SELECT_REBUILD -> {
            if (currentProject == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                val rebuildCandidates = currentProject.uploads.filter { it.type == PendingMediaType.IMAGE }
                LazyColumn(
                    state = rebuildSelectListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Spacer(Modifier.height(14.dp))

                        MiniChip(
                            text = "返回项目",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { viewModel.pageState = ContentPageState.PROJECT_DETAIL },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(14.dp))
                        Text("选择重建素材", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "请在素材文件夹中勾选参与重建的素材",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(18.dp))
                    }

                    if (rebuildCandidates.isEmpty()) {
                        item {
                            Text(
                                "当前项目没有可用于重建的图片素材，请先在素材文件夹中上传图片。",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    } else {
                        items(rebuildCandidates) { item ->
                            SelectableUploadRow(
                                item = item,
                                selected = currentProject.selectedRebuildUris.contains(item.uri.toString()),
                                onToggle = { toggleRebuildMedia(item.uri) },
                                onPreview = { viewModel.previewItem = item }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        PrimaryPillButton(
                            text = "确认重建选择",
                            onClick = {
                                viewModel.pageState = ContentPageState.PROJECT_DETAIL
                            },
                        )
                    }
                }
            }
        }

        ContentPageState.MEDIA_FOLDER_SELECT_PARSE -> {
            if (currentProject == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                val parseCandidates = if (currentProject.parseMode == ParseMode.EXPLODED_GUIDE) {
                    currentProject.uploads.filter { it.type == PendingMediaType.IMAGE }
                } else {
                    currentProject.uploads.filter { it.type == PendingMediaType.VIDEO }
                }
                LazyColumn(
                    state = parseSelectListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Spacer(Modifier.height(14.dp))

                        MiniChip(
                            text = "返回项目",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { viewModel.pageState = ContentPageState.PROJECT_DETAIL },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(14.dp))
                        Text("选择解析素材", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "请在素材文件夹中勾选参与解析的素材",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(18.dp))
                    }

                    if (parseCandidates.isEmpty()) {
                        item {
                            Text(
                                text = if (currentProject.parseMode == ParseMode.EXPLODED_GUIDE) {
                                    "当前项目没有可用于解析的说明书图片，请先在素材文件夹中上传图片。"
                                } else {
                                    "当前项目没有可用于解析的视频素材，请先在素材文件夹中上传视频。"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    } else {
                        items(parseCandidates) { item ->
                            SelectableUploadRow(
                                item = item,
                                selected = currentProject.selectedParseSourceUris.contains(item.uri.toString()),
                                onToggle = { toggleParseSource(item.uri) },
                                onPreview = { viewModel.previewItem = item }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        PrimaryPillButton(
                            text = "确认解析选择",
                            onClick = {
                                viewModel.pageState = ContentPageState.PROJECT_DETAIL
                            },
                        )
                    }
                }
            }
        }
    }

    if (showCreateProjectDialog) {
        CreateProjectDialog(
            onDismiss = { viewModel.showCreateProjectDialog = false },
            onConfirm = { name ->
                val finalName = viewModel.createProject(name)
                Toast.makeText(context, "已创建项目：$finalName", Toast.LENGTH_SHORT).show()
                viewModel.showCreateProjectDialog = false
            }
        )
    }

    if (showRenameProjectDialog && currentProject != null) {
        RenameProjectDialog(
            currentName = currentProject.name,
            onDismiss = { viewModel.showRenameProjectDialog = false },
            onConfirm = { newName ->
                if (viewModel.renameCurrentProject(newName)) {
                    Toast.makeText(context, "项目名称已更新", Toast.LENGTH_SHORT).show()
                }
                viewModel.showRenameProjectDialog = false
            }
        )
    }

    if (showEntrySheet && currentProject != null) {
        UploadEntrySheet(
            onDismiss = { viewModel.showEntrySheet = false },
            onUploadClick = {
                viewModel.showEntrySheet = false
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            onCaptureClick = {
                viewModel.showEntrySheet = false
                launchTakePhoto()
            }
        )
    }

    if (previewItem != null) {
        MediaPreviewSheet(
            item = previewItem,
            onDismiss = { viewModel.previewItem = null }
        )
    }

    if (showDeleteProjectDialog && currentProject != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.showDeleteProjectDialog = false },
            title = { Text("删除项目") },
            text = { Text("确认删除当前项目吗？删除后将无法恢复。") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.showDeleteProjectDialog = false
                        viewModel.deleteCurrentProject()
                    }
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { viewModel.showDeleteProjectDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

