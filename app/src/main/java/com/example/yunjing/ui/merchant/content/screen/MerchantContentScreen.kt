package com.example.yunjing.ui.merchant.content.screen

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.yunjing.ui.merchant.common.component.MiniChip
import com.example.yunjing.ui.merchant.common.component.PrimaryPillButton
import com.example.yunjing.ui.merchant.common.component.SoftCard
import com.example.yunjing.ui.merchant.content.component.CreateProjectDialog
import com.example.yunjing.ui.merchant.content.component.DeleteMediaDialog
import com.example.yunjing.ui.merchant.content.component.MaterialFolderCard
import com.example.yunjing.ui.merchant.content.component.ProgressBlock
import com.example.yunjing.ui.merchant.content.component.ProjectListItem
import com.example.yunjing.ui.merchant.content.component.ProjectSettingMenu
import com.example.yunjing.ui.merchant.content.component.RenameProjectDialog
import com.example.yunjing.ui.merchant.content.component.UploadEntrySheet
import com.example.yunjing.ui.merchant.content.component.WorkbenchSectionCard
import com.example.yunjing.ui.merchant.content.media.createImageUri
import com.example.yunjing.ui.merchant.content.media.createVideoUri
import com.example.yunjing.ui.merchant.content.model.ContentPageState
import com.example.yunjing.ui.merchant.content.model.MerchantContentProject
import com.example.yunjing.ui.merchant.content.model.ParseMode
import com.example.yunjing.ui.merchant.content.viewmodel.MerchantContentViewModel
import com.example.yunjing.ui.merchant.model.ProjectMediaAssetDto
import com.example.yunjing.ui.merchant.model.ProjectModelAssetDto
import com.example.yunjing.ui.pressClick
import com.example.yunjing.ui.unity.canOpenUnityPlayer
import com.example.yunjing.ui.unity.createUnityPlayerIntent
import kotlinx.coroutines.delay

/**
 * 本文件负责作为 merchant 内容库页面的主入口，统一处理状态读取、事件调度和页面分发。
 */

@Composable
fun MerchantContentScreen(
    viewModel: MerchantContentViewModel,
    initialProjectId: Long? = null
) {
    /**
     * 这个函数负责连接内容库 ViewModel 与各个子页面，并维持上传、权限、预览等跨页面状态。
     */
    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val projectDetailListState = rememberLazyListState()
    val mediaManageListState = rememberLazyListState()
    val rebuildSelectListState = rememberLazyListState()
    val parseSelectListState = rememberLazyListState()

    val projects = viewModel.projects
    val pageState = viewModel.pageState
    val currentProject = viewModel.currentProject()
    val runtime = currentProject?.let { viewModel.runtimeStateOf(it.id) }

    LaunchedEffect(initialProjectId, projects.size) {
        val targetId = initialProjectId ?: return@LaunchedEffect
        if (projects.any { it.id == targetId }) {
            viewModel.openProjectFromDashboard(targetId)
        }
    }

    val showCreateProjectDialog = viewModel.showCreateProjectDialog
    val showRenameProjectDialog = viewModel.showRenameProjectDialog
    val showEntrySheet = viewModel.showEntrySheet

    var previewMedia by remember { mutableStateOf<Pair<ProjectMediaAssetDto, Uri?>?>(null) }
    var pendingDeleteMedia by remember { mutableStateOf<ProjectMediaAssetDto?>(null) }
    var explodedImagePreviewUrl by remember { mutableStateOf<String?>(null) }

    val captureImageUri = remember { mutableStateOf<Uri?>(null) }
    val captureVideoUri = remember { mutableStateOf<Uri?>(null) }

    var pendingCameraAction by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val projectId = currentProject?.id ?: return@rememberLauncherForActivityResult
        val uri = captureImageUri.value ?: return@rememberLauncherForActivityResult

        if (success) {
            viewModel.uploadMedia(
                context = context,
                projectId = projectId,
                assetType = "IMAGE",
                uri = uri
            )
            Toast.makeText(context, "拍照上传成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "拍照失败或已取消", Toast.LENGTH_SHORT).show()
        }
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        val projectId = currentProject?.id ?: return@rememberLauncherForActivityResult
        val uri = captureVideoUri.value ?: return@rememberLauncherForActivityResult

        if (success) {
            viewModel.uploadMedia(
                context = context,
                projectId = projectId,
                assetType = "VIDEO",
                uri = uri
            )
            Toast.makeText(context, "拍摄视频上传成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "视频拍摄失败或已取消", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        val projectId = currentProject?.id ?: return@rememberLauncherForActivityResult
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        uris.forEach { uri ->
            viewModel.uploadMedia(
                context = context,
                projectId = projectId,
                assetType = "IMAGE",
                uri = uri
            )
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        val projectId = currentProject?.id ?: return@rememberLauncherForActivityResult
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        uris.forEach { uri ->
            viewModel.uploadMedia(
                context = context,
                projectId = projectId,
                assetType = "VIDEO",
                uri = uri
            )
        }
    }

    fun launchCameraImageCapture() {
        val uri = createImageUri(context)
        captureImageUri.value = uri
        takePictureLauncher.launch(uri)
    }

    fun launchCameraVideoCapture() {
        val uri = createVideoUri(context)
        captureVideoUri.value = uri
        captureVideoLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            when (pendingCameraAction) {
                "image" -> launchCameraImageCapture()
                "video" -> launchCameraVideoCapture()
            }
        } else {
            Toast.makeText(context, "未授予相机权限，无法拍摄", Toast.LENGTH_SHORT).show()
        }
        pendingCameraAction = null
    }

    fun ensureCameraPermissionAndLaunch(action: String) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            when (action) {
                "image" -> launchCameraImageCapture()
                "video" -> launchCameraVideoCapture()
            }
        } else {
            pendingCameraAction = action
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun openUnityPlayer(
        publishCode: String?,
        tutorialVideoUrl: String?,
        tutorialTitle: String?
    ) {
        try {
            val intent = context.createUnityPlayerIntent(
                publishCode = publishCode,
                tutorialVideoUrl = tutorialVideoUrl,
                tutorialTitle = tutorialTitle
            )
            if (!context.canOpenUnityPlayer(intent)) {
                Toast.makeText(context, "鏈壘鍒?Unity 椤甸潰", Toast.LENGTH_SHORT).show()
                return
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

    LaunchedEffect(currentProject?.id, runtime?.isRealRebuilding) {
        val projectId = currentProject?.id ?: return@LaunchedEffect
        val state = runtime ?: return@LaunchedEffect
        if (!state.isRealRebuilding) return@LaunchedEffect

        val progressPoints = listOf(
            0.08f, 0.18f, 0.30f, 0.42f, 0.53f,
            0.62f, 0.70f, 0.77f, 0.83f, 0.88f,
            0.91f, 0.94f, 0.96f, 0.975f, 0.985f,
            0.992f, 0.996f, 0.998f, 0.999f, 1f
        )

        val delays = listOf(
            400L, 500L, 600L, 700L, 900L,
            1200L, 1500L, 1800L, 2200L, 2600L,
            3000L, 3500L, 4000L, 4500L, 5000L,
            5500L, 6000L, 6500L, 7000L, 7500L
        )

        for (i in progressPoints.indices) {
            delay(delays[i])
            viewModel.updateRuntimeState(projectId) {
                it.copy(rebuildProgress = progressPoints[i])
            }
        }

        viewModel.finishRebuild(projectId)
        Toast.makeText(context, "重建完成", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(currentProject?.id, runtime?.realParsing) {
        val projectId = currentProject?.id ?: return@LaunchedEffect
        val state = runtime ?: return@LaunchedEffect
        if (!state.realParsing) return@LaunchedEffect

        val progressPoints = listOf(
            0.08f, 0.18f, 0.30f, 0.42f, 0.53f,
            0.62f, 0.70f, 0.77f, 0.83f, 0.88f,
            0.91f, 0.94f, 0.96f, 0.975f, 0.985f,
            0.992f, 0.996f, 0.998f, 0.999f, 1f
        )

        val delays = listOf(
            400L, 500L, 600L, 700L, 900L,
            1200L, 1500L, 1800L, 2200L, 2600L,
            3000L, 3500L, 4000L, 4500L, 5000L,
            5500L, 6000L, 6500L, 7000L, 7500L
        )

        for (i in progressPoints.indices) {
            delay(delays[i])
            viewModel.updateRuntimeState(projectId) {
                it.copy(parseProgress = progressPoints[i])
            }
        }

        viewModel.finishParse(projectId)
        Toast.makeText(context, "解析完成", Toast.LENGTH_SHORT).show()
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

                items(projects, key = { it.id }) { project ->
                    ProjectListItem(
                        project = MerchantContentProject(
                            id = project.id,
                            name = project.projectName,
                            summary = project.projectDesc ?: "点击进入项目工作台",
                            status = viewModel.stageLabel(project.id)
                        ),
                        onClick = {
                            viewModel.selectProject(project.id)
                            viewModel.pageState = ContentPageState.PROJECT_DETAIL
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        ContentPageState.PROJECT_DETAIL -> {
            if (currentProject == null || runtime == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                val mediaAssets = viewModel.currentMediaAssets
                val modelAssets = viewModel.currentModelAssets
                val detailProject = viewModel.currentProjectDetail?.project
                val explodedImageUrl = detailProject?.explodedImageUrl
                val tutorialVideoUrl = detailProject?.tutorialVideoUrl
                val tutorialTitle = detailProject?.tutorialTitle
                val parseResultText = detailProject?.parseResultText
                val canPublish =
                    (detailProject?.hasRebuildOutput == 1) ||
                            !explodedImageUrl.isNullOrBlank() ||
                            !tutorialVideoUrl.isNullOrBlank()

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
                                onRename = { viewModel.showRenameProjectDialog = true },
                                onDelete = { viewModel.showDeleteProjectDialog = true }
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(currentProject.projectName, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${currentProject.projectDesc ?: "项目内容工作台"} · ${
                                if (currentProject.publishStatus == "PUBLISHED") "已发布" else runtime.publishStatus
                            }",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(18.dp))

                        WorkbenchSectionCard(
                            title = "素材管理",
                            desc = "当前项目的图片与视频素材统一存放在素材文件夹中，支持继续添加。",
                            actionArea = {}
                        ) {
                            MaterialFolderCard(
                                "素材文件夹",
                                mediaCount = mediaAssets.size,
                                onClick = { viewModel.pageState = ContentPageState.MEDIA_FOLDER_MANAGE }
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        WorkbenchSectionCard(
                            title = "模型管理",
                            desc = "模型文件夹用于存放重建完成后可供解析选择的模型文件，点击重建完成后应能在此查看。",
                            actionArea = {}
                        ) {
                            MaterialFolderCard(
                                "模型文件夹",
                                mediaCount = modelAssets.size,
                                onClick = {
                                    viewModel.loadModels(currentProject.id)
                                    viewModel.pageState = ContentPageState.MODEL_FOLDER_MANAGE
                                }
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        WorkbenchSectionCard(
                            title = "重建",
                            desc = "从素材文件夹中勾选图片素材进行重建，完成后模型文件将进入模型文件夹，供后续解析选择。",
                            actionArea = {}
                        ) {
                            MiniChip(
                                text = "进入素材文件夹选择重建图片",
                                icon = Icons.Filled.ViewInAr,
                                onClick = {
                                    if (mediaAssets.none { it.assetType.equals("IMAGE", true) }) {
                                        Toast.makeText(context, "当前项目暂无图片素材，请先上传", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.pageState = ContentPageState.MEDIA_FOLDER_SELECT_REBUILD
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (runtime.selectedRebuildAssetIds.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "已选择 ${runtime.selectedRebuildAssetIds.size} 项重建素材",
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
                                        onClick = { viewModel.clearRebuildSelection(currentProject.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    MiniChip(
                                        text = "开始重建",
                                        icon = Icons.Filled.ViewInAr,
                                        onClick = {
                                            if (runtime.selectedRebuildAssetIds.isEmpty()) {
                                                Toast.makeText(context, "请先选择图片素材后再开始重建", Toast.LENGTH_SHORT).show()
                                            } else if (!runtime.isRealRebuilding) {
                                                viewModel.startRebuild(currentProject.id)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            if (runtime.isRealRebuilding) {
                                Spacer(Modifier.height(12.dp))
                                ProgressBlock(
                                    title = "3D 重建中",
                                    progress = runtime.rebuildProgress,
                                    hint = "请稍后"
                                )
                            }

                            if (runtime.realRebuildResult != null) {
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("重建结果", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            runtime.realRebuildResult,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            if (modelAssets.isEmpty()) "点击重建后可看到预置模型文件夹中的模型" else "当前项目已有 ${modelAssets.size} 个模型结果",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        WorkbenchSectionCard(
                            title = "解析",
                            desc = "解析交互，说明书解析/视频教程解析两种流程。",
                            actionArea = {
                                MiniChip(
                                    text = if (runtime.parseMode == ParseMode.EXPLODED_GUIDE) "切到视频教程解析" else "切到爆炸图解析",
                                    icon = Icons.Filled.Description,
                                    onClick = { viewModel.toggleParseMode(currentProject.id) }
                                )
                            }
                        ) {
                            Text(
                                text = if (runtime.parseMode == ParseMode.EXPLODED_GUIDE) {
                                    "当前模式：说明书图片 + 模型 → 爆炸图"
                                } else {
                                    "当前模式：视频 + 模型 → 教程播放器"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(12.dp))

                            MiniChip(
                                text = if (runtime.parseMode == ParseMode.EXPLODED_GUIDE) {
                                    "进入素材文件夹选择说明书图片"
                                } else {
                                    "进入素材文件夹选择解析视频"
                                },
                                icon = Icons.Filled.Description,
                                onClick = {
                                    val hasCandidate =
                                        if (runtime.parseMode == ParseMode.EXPLODED_GUIDE) {
                                            mediaAssets.any { it.assetType.equals("IMAGE", true) }
                                        } else {
                                            mediaAssets.any { it.assetType.equals("VIDEO", true) }
                                        }
                                    if (!hasCandidate) {
                                        Toast.makeText(
                                            context,
                                            "当前项目暂无可解析素材，请先上传",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        viewModel.pageState =
                                            ContentPageState.MEDIA_FOLDER_SELECT_PARSE
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
                                    text = "从模型文件夹选择模型",
                                    icon = Icons.Filled.ViewInAr,
                                    onClick = {
                                        if (modelAssets.isEmpty()) {
                                            Toast.makeText(
                                                context,
                                                "当前暂无模型，请先完成重建",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            viewModel.pageState =
                                                ContentPageState.MODEL_FOLDER_SELECT_PARSE
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                MiniChip(
                                    text = "清空解析选择",
                                    icon = Icons.Filled.ClearAll,
                                    onClick = { viewModel.clearParseSelection(currentProject.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            PrimaryPillButton(
                                text = "开始解析",
                                onClick = {
                                    if (runtime.selectedParseSourceAssetIds.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            if (runtime.parseMode == ParseMode.EXPLODED_GUIDE) "请先选择说明书图片" else "请先选择解析视频",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (runtime.selectedParseModelIds.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "请至少选择一个模型文件",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (!runtime.realParsing) {
                                        viewModel.startParse(currentProject.id)
                                    }
                                }
                            )

                            if (runtime.realParsing) {
                                Spacer(Modifier.height(12.dp))
                                ProgressBlock(
                                    title = "内容解析中",
                                    progress = runtime.parseProgress,
                                    hint = "请稍后"
                                )
                            }


                            if (!explodedImageUrl.isNullOrBlank()) {
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("爆炸图结果", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            parseResultText ?: "已生成爆炸图说明结果，可继续查看或发布。",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(Modifier.height(12.dp))
                                        AsyncImage(
                                            model = normalizePreviewUrl(explodedImageUrl),
                                            contentDescription = "爆炸图缩略图",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(Modifier.height(12.dp))
                                        PrimaryPillButton(
                                            text = "预览爆炸图",
                                            onClick = {
                                                explodedImagePreviewUrl = normalizePreviewUrl(explodedImageUrl)
                                            }
                                        )
                                    }
                                }
                            }

                            if (!tutorialVideoUrl.isNullOrBlank()) {
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            tutorialTitle ?: "教程播放器结果",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            parseResultText
                                                ?: "已生成教程播放器入口结果，可继续打开。",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        PrimaryPillButton(
                                            text = "打开教程播放器",
                                            onClick = {
                                                openUnityPlayer(
                                                    publishCode = detailProject?.publishCode,
                                                    tutorialVideoUrl = tutorialVideoUrl,
                                                    tutorialTitle = tutorialTitle
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))

                        PrimaryPillButton(
                            text = if (canPublish) {
                                if (currentProject.publishStatus == "PUBLISHED") "重新生成发布二维码" else "发布当前项目"
                            } else {
                                "发布当前项目（需先生成结果）"
                            },
                            onClick = {
                                if (!canPublish) {
                                    Toast.makeText(context, "请先完成重建或解析中的至少一项成果", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.publishProject(
                                        projectId = currentProject.id,
                                        onSuccess = {
                                            Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        )

                        val publishQrBase64 = detailProject?.qrCodeBase64
                        val publishUrl = detailProject?.publishUrl
                        val qrBitmap = remember(publishQrBase64) { decodeBase64ToImageBitmap(publishQrBase64) }

                        if (!publishQrBase64.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            SoftCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("项目发布二维码", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(8.dp))

                                    if (qrBitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = qrBitmap,
                                            contentDescription = "项目发布二维码",
                                            modifier = Modifier
                                                .size(220.dp)
                                                .align(Alignment.CenterHorizontally)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Text(
                                            text = "二维码加载失败，请重新发布一次",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = publishUrl ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        ContentPageState.MODEL_FOLDER_MANAGE -> {
            if (currentProject == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                val modelAssets = viewModel.currentModelAssets

                LazyColumn(
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
                        Text("模型文件夹", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "重建完成后的模型文件会显示在这里，可用于后续解析选择。",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(18.dp))
                    }

                    if (modelAssets.isEmpty()) {
                        item {
                            SoftCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "当前暂无模型文件。请先完成重建，或检查后端是否已返回重建好的模型。",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(modelAssets, key = { it.id }) { item ->
                            BackendModelRow(
                                item = item,
                                onPreview = { viewModel.openModelPreview(item)}
                            )
                            Spacer(Modifier.height(10.dp))
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
                val mediaAssets = viewModel.currentMediaAssets
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
                        Text("统一管理当前项目内的图片与视频素材", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(18.dp))
                    }

                    if (mediaAssets.isEmpty()) {
                        item {
                            Text("当前项目暂无素材，请点击“添加素材”继续上传。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(mediaAssets, key = { it.id }) { item ->
                            BackendMediaRow(
                                item = item,
                                onPreview = {
                                    previewMedia = item to viewModel.localPreviewUriOf(item.id)
                                },
                                onDelete = {
                                    pendingDeleteMedia = item
                                }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        ContentPageState.MEDIA_FOLDER_SELECT_REBUILD -> {
            if (currentProject == null || runtime == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                val rebuildCandidates = viewModel.currentMediaAssets.filter { it.assetType.equals("IMAGE", true) }
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
                        Text("请在素材文件夹中勾选参与重建的图片素材", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(18.dp))
                    }

                    items(rebuildCandidates, key = { it.id }) { item ->
                        BackendSelectableMediaRow(
                            item = item,
                            selected = runtime.selectedRebuildAssetIds.contains(item.id),
                            onToggle = { viewModel.toggleRebuildAsset(currentProject.id, item.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        PrimaryPillButton(
                            text = "确认重建选择",
                            onClick = { viewModel.pageState = ContentPageState.PROJECT_DETAIL }
                        )
                    }
                }
            }
        }

        ContentPageState.MODEL_FOLDER_SELECT_PARSE -> {
            if (currentProject == null || runtime == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                val modelAssets = viewModel.currentModelAssets

                LazyColumn(
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
                        Text("选择解析模型", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "请在模型文件夹中勾选参与解析的模型文件。",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(18.dp))
                    }

                    items(modelAssets, key = { it.id }) { item ->
                        BackendSelectableModelRow(
                            item = item,
                            selected = runtime.selectedParseModelIds.contains(item.id),
                            onToggle = { viewModel.toggleParseModel(currentProject.id, item.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        PrimaryPillButton(
                            text = "确认模型选择",
                            onClick = { viewModel.pageState = ContentPageState.PROJECT_DETAIL }
                        )
                    }
                }
            }
        }

        ContentPageState.MEDIA_FOLDER_SELECT_PARSE -> {
            if (currentProject == null || runtime == null) {
                viewModel.selectedProjectId = null
                viewModel.pageState = ContentPageState.PROJECT_LIST
            } else {
                val parseCandidates = if (runtime.parseMode == ParseMode.EXPLODED_GUIDE) {
                    viewModel.currentMediaAssets.filter { it.assetType.equals("IMAGE", true) }
                } else {
                    viewModel.currentMediaAssets.filter { it.assetType.equals("VIDEO", true) }
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
                        Text("请在素材文件夹中勾选参与解析的素材", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(18.dp))
                    }

                    items(parseCandidates, key = { it.id }) { item ->
                        BackendSelectableMediaRow(
                            item = item,
                            selected = runtime.selectedParseSourceAssetIds.contains(item.id),
                            onToggle = { viewModel.toggleParseAsset(currentProject.id, item.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        PrimaryPillButton(
                            text = "确认解析选择",
                            onClick = { viewModel.pageState = ContentPageState.PROJECT_DETAIL }
                        )
                    }
                }
            }
        }

        ContentPageState.MODEL_PREVIEW -> {
            val previewModel = viewModel.previewModel

            if (currentProject == null || previewModel == null) {
                viewModel.closeModelPreview()
            } else {
                ModelPreviewContent(
                    model = previewModel,
                    onBack = { viewModel.closeModelPreview() }
                )
            }
        }
    }

    if (showCreateProjectDialog) {
        CreateProjectDialog(
            onDismiss = { viewModel.showCreateProjectDialog = false },
            onConfirm = { name ->
                viewModel.createProject(
                    name = name,
                    desc = null,
                    onSuccess = {
                        Toast.makeText(context, "已创建项目：$name", Toast.LENGTH_SHORT).show()
                        viewModel.showCreateProjectDialog = false
                        viewModel.pageState = ContentPageState.PROJECT_LIST
                        viewModel.loadProjects()
                    }
                )
            }
        )
    }

    if (showRenameProjectDialog && currentProject != null) {
        RenameProjectDialog(
            currentName = currentProject.projectName,
            onDismiss = { viewModel.showRenameProjectDialog = false },
            onConfirm = { newName ->
                viewModel.renameProject(
                    projectId = currentProject.id,
                    newName = newName,
                    onSuccess = {
                        Toast.makeText(context, "项目已重命名", Toast.LENGTH_SHORT).show()
                        viewModel.showRenameProjectDialog = false
                    }
                )
            }
        )
    }

    if (viewModel.showDeleteProjectDialog && currentProject != null) {
        DeleteProjectDialog(
            projectName = currentProject.projectName,
            onDismiss = { viewModel.showDeleteProjectDialog = false },
            onConfirm = {
                val deletedId = currentProject.id
                viewModel.deleteProject(
                    projectId = deletedId,
                    onSuccess = {
                        viewModel.removeRuntimeState(deletedId)
                        viewModel.showDeleteProjectDialog = false
                        viewModel.selectedProjectId = null
                        viewModel.pageState = ContentPageState.PROJECT_LIST
                        Toast.makeText(context, "项目已删除", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (showEntrySheet) {
        UploadEntrySheet(
            onDismiss = { viewModel.showEntrySheet = false },
            onUploadImageClick = {
                viewModel.showEntrySheet = false
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onUploadVideoClick = {
                viewModel.showEntrySheet = false
                videoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onCaptureImageClick = {
                viewModel.showEntrySheet = false
                ensureCameraPermissionAndLaunch("image")
            },
            onCaptureVideoClick = {
                viewModel.showEntrySheet = false
                ensureCameraPermissionAndLaunch("video")
            }
        )
    }

    pendingDeleteMedia?.let { media ->
        DeleteMediaDialog(
            fileName = media.fileName,
            onDismiss = { pendingDeleteMedia = null },
            onConfirm = {
                val projectId = currentProject?.id ?: return@DeleteMediaDialog
                val mediaId = media.id

                viewModel.deleteMedia(
                    projectId = projectId,
                    mediaId = mediaId,
                    onSuccess = {
                        if (previewMedia?.first?.id == mediaId) {
                            previewMedia = null
                        }
                        Toast.makeText(context, "素材已删除", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    previewMedia?.let { (media, localUri) ->
        BackendMediaPreviewDialog(
            item = media,
            localUri = localUri,
            onDismiss = { previewMedia = null }
        )
    }

    explodedImagePreviewUrl?.let { imageUrl ->
        ExplodedImagePreviewDialog(
            imageUrl = imageUrl,
            onDismiss = { explodedImagePreviewUrl = null }
        )
    }
}
