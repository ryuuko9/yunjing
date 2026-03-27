package com.example.yunjing.ui.merchant.screen

import android.content.ActivityNotFoundException
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yunjing.ui.merchant.component.CreateProjectDialog
import com.example.yunjing.ui.merchant.component.MaterialFolderCard
import com.example.yunjing.ui.merchant.component.MiniChip
import com.example.yunjing.ui.merchant.component.PrimaryPillButton
import com.example.yunjing.ui.merchant.component.ProgressBlock
import com.example.yunjing.ui.merchant.component.ProjectListItem
import com.example.yunjing.ui.merchant.component.ProjectSettingMenu
import com.example.yunjing.ui.merchant.component.RenameProjectDialog
import com.example.yunjing.ui.merchant.component.SoftCard
import com.example.yunjing.ui.merchant.component.UploadEntrySheet
import com.example.yunjing.ui.merchant.component.WorkbenchSectionCard
import com.example.yunjing.ui.merchant.model.ContentPageState
import com.example.yunjing.ui.merchant.model.ParseMode
import com.example.yunjing.ui.merchant.model.ProjectMediaAssetDto
import com.example.yunjing.ui.merchant.viewmodel.MerchantContentViewModel
import com.example.yunjing.ui.pressClick
import kotlinx.coroutines.delay
import android.net.Uri
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.yunjing.ui.merchant.model.ProjectModelAssetDto
import java.io.File
import coil.compose.AsyncImage
import com.example.yunjing.ui.merchant.util.createImageUri
import com.example.yunjing.ui.merchant.util.createVideoUri
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.request.ImageRequest
import com.example.yunjing.ui.merchant.component.DeleteMediaDialog

import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.model.ModelInstance
import com.google.android.filament.LightManager
import io.github.sceneview.node.LightNode


import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

import androidx.compose.ui.graphics.Color
private const val UNITY_PLAYER_PACKAGE = "com.example.yunjing.tutorialplayer"

/**
 * 这是“旧版 UI + 后端项目数据”的合并版本。
 *
 * 设计原则：
 * 1. 项目列表 / 项目详情 / 素材文件夹 / 重建选择 / 解析选择，恢复旧版页面节奏。
 * 2. 项目基础数据全部来自后端。
 * 3. fake 重建、fake 解析、进度条、结果持久化走 ViewModel 中的本地运行态，不污染后端表结构。
 * 4. 重命名、删除项目改为真实后端接口。
 *
 * 要求 ViewModel 补充的方法见文件底部注释。
 */
@Composable
fun MerchantContentScreen(
    viewModel: MerchantContentViewModel
) {
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

    val showCreateProjectDialog = viewModel.showCreateProjectDialog
    val showRenameProjectDialog = viewModel.showRenameProjectDialog
    val showDeleteProjectDialog = viewModel.showDeleteProjectDialog
    val showEntrySheet = viewModel.showEntrySheet

    var previewMedia by remember { mutableStateOf<Pair<ProjectMediaAssetDto, Uri?>?>(null) }
    var pendingDeleteMedia by remember { mutableStateOf<ProjectMediaAssetDto?>(null) }

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

    fun openUnityPlayer() {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(UNITY_PLAYER_PACKAGE)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(context, "教程播放器占位包未接入", Toast.LENGTH_SHORT).show()
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "未找到教程播放器应用", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(currentProject?.id, runtime?.isFakeRebuilding) {
        val projectId = currentProject?.id ?: return@LaunchedEffect
        val state = runtime ?: return@LaunchedEffect
        if (!state.isFakeRebuilding) return@LaunchedEffect

        repeat(20) { index ->
            delay(140)
            viewModel.updateRuntimeState(projectId) {
                it.copy(rebuildProgress = (index + 1) / 20f)
            }
        }

        viewModel.finishFakeRebuild(projectId)
        Toast.makeText(context, "重建完成", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(currentProject?.id, runtime?.isFakeParsing) {
        val projectId = currentProject?.id ?: return@LaunchedEffect
        val state = runtime ?: return@LaunchedEffect
        if (!state.isFakeParsing) return@LaunchedEffect

        repeat(24) { index ->
            delay(120)
            viewModel.updateRuntimeState(projectId) {
                it.copy(parseProgress = (index + 1) / 24f)
            }
        }

        viewModel.finishFakeParse(projectId)
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
                        project = com.example.yunjing.ui.merchant.model.MerchantContentProject(
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
                val canPublish = runtime.fakeRebuildResult != null ||
                        runtime.fakeExplodedGuideResult != null ||
                        runtime.fakeVideoGuideResult != null

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
                            "${currentProject.projectDesc ?: "项目内容工作台"} · ${runtime.publishStatus}",
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
                                        onClick = { viewModel.clearFakeRebuildSelection(currentProject.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    MiniChip(
                                        text = "开始重建",
                                        icon = Icons.Filled.ViewInAr,
                                        onClick = {
                                            if (runtime.selectedRebuildAssetIds.isEmpty()) {
                                                Toast.makeText(context, "请先选择图片素材后再开始重建", Toast.LENGTH_SHORT).show()
                                            } else if (!runtime.isFakeRebuilding) {
                                                viewModel.startFakeRebuild(currentProject.id)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            if (runtime.isFakeRebuilding) {
                                Spacer(Modifier.height(12.dp))
                                ProgressBlock(
                                    title = "3D 重建中",
                                    progress = runtime.rebuildProgress,
                                    hint = "当前为 fake 进度条，后续可接真实重建后端"
                                )
                            }

                            if (runtime.fakeRebuildResult != null) {
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("重建结果", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            runtime.fakeRebuildResult,
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
                            desc = "恢复旧版解析交互，保留说明书解析 / 视频教程解析两种 fake 流程。",
                            actionArea = {
                                MiniChip(
                                    text = if (runtime.parseMode == ParseMode.EXPLODED_GUIDE) "切到视频教程解析" else "切到爆炸图解析",
                                    icon = Icons.Filled.Description,
                                    onClick = { viewModel.toggleFakeParseMode(currentProject.id) }
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
                                    val hasCandidate = if (runtime.parseMode == ParseMode.EXPLODED_GUIDE) {
                                        mediaAssets.any { it.assetType.equals("IMAGE", true) }
                                    } else {
                                        mediaAssets.any { it.assetType.equals("VIDEO", true) }
                                    }
                                    if (!hasCandidate) {
                                        Toast.makeText(context, "当前项目暂无可解析素材，请先上传", Toast.LENGTH_SHORT).show()
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
                                    text = "从模型文件夹选择模型",
                                    icon = Icons.Filled.ViewInAr,
                                    onClick = {
                                        if (modelAssets.isEmpty()) {
                                            Toast.makeText(context, "当前暂无模型，请先完成重建", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.pageState = ContentPageState.MODEL_FOLDER_SELECT_PARSE
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                MiniChip(
                                    text = "清空解析选择",
                                    icon = Icons.Filled.ClearAll,
                                    onClick = { viewModel.clearFakeParseSelection(currentProject.id) },
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
                                        Toast.makeText(context, "请至少选择一个模型文件", Toast.LENGTH_SHORT).show()
                                    } else if (!runtime.isFakeParsing) {
                                        viewModel.startFakeParse(currentProject.id)
                                    }
                                }
                            )

                            if (runtime.isFakeParsing) {
                                Spacer(Modifier.height(12.dp))
                                ProgressBlock(
                                    title = "内容解析中",
                                    progress = runtime.parseProgress,
                                    hint = "当前为 fake 解析进度，后续可接真实生成服务"
                                )
                            }

                            runtime.fakeExplodedGuideResult?.let {
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("爆炸图结果", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            it,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        runtime.fakeExplodedImageUrl?.let { imageUrl ->
                                            Spacer(Modifier.height(12.dp))
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = "爆炸图",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            runtime.fakeVideoGuideResult?.let {
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("教程播放器结果", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            it,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        PrimaryPillButton(
                                            text = "打开 Unity 教程播放器",
                                            onClick = { openUnityPlayer() }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        PrimaryPillButton(
                            text = if (canPublish) "发布当前项目" else "发布当前项目（需先生成结果）",
                            onClick = {
                                if (!canPublish) {
                                    Toast.makeText(context, "请先完成重建或解析中的至少一项成果", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.markFakePublish(currentProject.id)
                                    Toast.makeText(context, "发布成功（当前为本地占位逻辑）", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        ContentPageState.MODEL_FOLDER_MANAGE -> {
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
                                    "当前暂无模型文件。请先完成重建，或检查后端是否已返回 fake-model-repo/default 下的模型。",
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
                            onToggle = { viewModel.toggleFakeRebuildAsset(currentProject.id, item.id) }
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
                            onToggle = { viewModel.toggleFakeParseModel(currentProject.id, item.id) }
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
                            onToggle = { viewModel.toggleFakeParseAsset(currentProject.id, item.id) }
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
            fileName = media.fileName ?: "未命名素材",
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
                        pendingDeleteMedia = null
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
}

@Composable
fun DeleteProjectDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除项目") },
        text = { Text("确认删除项目“$projectName”吗？删除后不可恢复。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun BackendMediaRow(
    item: ProjectMediaAssetDto,
    onPreview: () -> Unit,
    onDelete: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.fileName ?: "未命名素材",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (item.assetType.equals("VIDEO", true)) "视频素材" else "图片素材",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPreview) {
                    Text("预览")
                }

                Spacer(Modifier.width(4.dp))

                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFFFF3B30).copy(alpha = 0.12f))
                        .pressClick(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ClearAll,
                        contentDescription = "删除素材",
                        tint = androidx.compose.ui.graphics.Color(0xFFFF3B30)
                    )
                }
            }
        }
    }
}
@Composable
fun BackendSelectableMediaRow(
    item: ProjectMediaAssetDto,
    selected: Boolean,
    onToggle: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onToggle),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.fileName ?: "未命名素材", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "图片素材",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
fun BackendModelRow(
    item: ProjectModelAssetDto,
    onPreview: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.modelName ?: "未命名模型", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    item.fileUrl ?: "模型路径为空",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onPreview) {
                Text("预览")
            }
        }
    }
}

@Composable
fun BackendSelectableModelRow(
    item: ProjectModelAssetDto,
    selected: Boolean,
    onToggle: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onToggle),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.modelName ?: "未命名模型", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    item.fileUrl ?: "模型路径为空",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
private fun BackendMediaPreviewDialog(
    item: ProjectMediaAssetDto,
    localUri: Uri?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val remoteUrl = remember(item.fileUrl) { normalizePreviewUrl(item.fileUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = {
            Text(item.fileName ?: "素材预览")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    item.assetType.equals("VIDEO", true) -> {
                        when {
                            localUri != null -> {
                                AndroidView(
                                    factory = { ctx ->
                                        android.widget.VideoView(ctx).apply {
                                            setVideoURI(localUri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                start()
                                            }
                                        }
                                    },
                                    update = { view ->
                                        view.setVideoURI(localUri)
                                        view.setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            view.start()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                )
                            }

                            !remoteUrl.isNullOrBlank() -> {
                                Text(
                                    "当前视频暂无本地缓存",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            else -> {
                                Text(
                                    "当前视频暂无可用预览地址",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    localUri != null -> {
                        AsyncImage(
                            model = localUri,
                            contentDescription = item.fileName ?: "图片预览",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    !remoteUrl.isNullOrBlank() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(remoteUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.fileName ?: "图片预览",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    else -> {
                        Text(
                            "当前图片暂无可用预览地址",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

private fun normalizePreviewUrl(rawUrl: String?): String? {
    if (rawUrl.isNullOrBlank()) return null

    return when {
        rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> {
            rawUrl.replace("localhost", "10.0.2.2")
        }

        rawUrl.startsWith("/") -> {
            "http://10.0.2.2:8080$rawUrl"
        }

        else -> rawUrl
    }
}
@Composable
private fun ModelPreviewContent(
    model: ProjectModelAssetDto,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val modelUrl = remember(model.fileUrl) { normalizeModelUrl(model.fileUrl) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraManipulator = rememberCameraManipulator()
    val environment = rememberEnvironment(engine)

    var modelInstance by remember(modelUrl) { mutableStateOf<ModelInstance?>(null) }
    var isLoading by remember(modelUrl) { mutableStateOf(false) }
    var loadError by remember(modelUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(modelUrl) {
        modelInstance = null
        loadError = null

        if (modelUrl.isNullOrBlank()) {
            loadError = "模型地址为空，无法预览"
            return@LaunchedEffect
        }

        isLoading = true

        try {
            val localFile = downloadModelToCache(context, modelUrl)

            if (!localFile.exists() || localFile.length() <= 0L) {
                loadError = "模型文件为空或不存在"
                return@LaunchedEffect
            }

            modelInstance = modelLoader.createModelInstance(localFile)
        } catch (e: Exception) {
            e.printStackTrace()
            loadError = e.message ?: "模型加载失败"
        } finally {
            isLoading = false
        }
    }

    val previewNode = remember(modelUrl, modelInstance) {
        modelInstance?.let { instance ->
            ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.2f,
                centerOrigin = Position(0.0f, 0.0f, 0.0f)
            ).apply {
                isEditable = true
            }
        }
    }

    val keyLight = remember(engine) {
        LightNode(
            engine = engine,
            type = LightManager.Type.DIRECTIONAL
        ) {
            color(1.0f, 1.0f, 1.0f)
            intensity(120_000.0f)
            direction(0.3f, -1.0f, -0.8f)
            castShadows(false)
        }
    }

    val fillLight = remember(engine) {
        LightNode(
            engine = engine,
            type = LightManager.Type.DIRECTIONAL
        ) {
            color(1.0f, 1.0f, 1.0f)
            intensity(80_000.0f)
            direction(-0.8f, -0.4f, 0.2f)
            castShadows(false)
        }
    }

    val backLight = remember(engine) {
        LightNode(
            engine = engine,
            type = LightManager.Type.DIRECTIONAL
        ) {
            color(1.0f, 1.0f, 1.0f)
            intensity(60_000.0f)
            direction(0.0f, -0.2f, 1.0f)
            castShadows(false)
        }
    }

    val bottomLight = remember(engine) {
        LightNode(
            engine = engine,
            type = LightManager.Type.DIRECTIONAL
        ) {
            color(1.0f, 1.0f, 1.0f)
            intensity(50_000.0f)
            direction(0.0f, 1.0f, 0.3f)
            castShadows(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        MiniChip(
            text = "返回模型文件夹",
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = model.modelName?.ifBlank { "模型预览" } ?: "模型预览",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = modelUrl ?: "模型地址为空",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = when {
                isLoading -> "模型加载中"
                loadError != null -> loadError ?: "模型加载失败"
                modelInstance != null -> "模型已加载"
                else -> "等待加载"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Scene(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                cameraManipulator = cameraManipulator,
                environment = environment
            ) {
                addChildNode(keyLight)
                addChildNode(fillLight)
                addChildNode(backLight)
                previewNode?.let { addChildNode(it) }
            }

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                loadError != null -> {
                    Text(
                        text = loadError ?: "模型加载失败",
                        color = Color.White
                    )
                }
            }
        }
    }
}

private suspend fun downloadModelToCache(
    context: android.content.Context,
    urlString: String
): File = withContext(Dispatchers.IO) {
    val modelDir = File(context.cacheDir, "model_preview_cache")
    if (!modelDir.exists()) {
        modelDir.mkdirs()
    }

    val fileName = buildString {
        append("preview_")
        append(urlString.hashCode())
        append(".glb")
    }

    val targetFile = File(modelDir, fileName)

    if (targetFile.exists() && targetFile.length() > 0L) {
        return@withContext targetFile
    }

    val url = URL(urlString)
    val connection = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 15000
        readTimeout = 15000
        requestMethod = "GET"
        doInput = true
        connect()
    }

    try {
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw IllegalStateException("模型下载失败，HTTP $responseCode")
        }

        BufferedInputStream(connection.inputStream).use { input ->
            FileOutputStream(targetFile).use { output ->
                val buffer = ByteArray(8 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count == -1) break
                    output.write(buffer, 0, count)
                }
                output.flush()
            }
        }

        if (!targetFile.exists() || targetFile.length() <= 0L) {
            throw IllegalStateException("模型下载完成，但本地文件为空")
        }

        targetFile
    } finally {
        connection.disconnect()
    }
}

private fun normalizeModelUrl(rawUrl: String?): String? {
    val value = rawUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    return when {
        value.startsWith("http://", ignoreCase = true) ||
                value.startsWith("https://", ignoreCase = true) -> {
            value
                .replace("localhost", "10.0.2.2")
                .replace("127.0.0.1", "10.0.2.2")
        }

        value.startsWith("/") -> {
            "http://10.0.2.2:8080$value"
        }

        else -> {
            "http://10.0.2.2:8080/$value"
        }
    }
}