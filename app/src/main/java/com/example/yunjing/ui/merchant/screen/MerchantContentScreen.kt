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

    val openModelsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val projectId = currentProject?.id ?: return@rememberLauncherForActivityResult
        val valid = uris.filter {
            val v = it.toString().lowercase()
            v.endsWith(".glb") || v.contains(".glb")
        }
        if (valid.isEmpty() && uris.isNotEmpty()) {
            Toast.makeText(context, "当前仅支持选择 .glb 模型文件", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        viewModel.appendLocalParseModels(projectId, valid)
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
                            status = "后端项目"
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
                            title = "重建",
                            desc = "从素材文件夹中选择图片素材，保留原先 fake 重建体验，但项目基础数据来自后端。",
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
                                    text = "导入模型",
                                    icon = Icons.Filled.ViewInAr,
                                    onClick = { openModelsLauncher.launch(arrayOf("*/*")) },
                                    modifier = Modifier.weight(1f)
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
                                    } else if (runtime.selectedParseModelUris.isEmpty()) {
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

                            runtime.fakeExplodedGuideResult?.let { result ->
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("爆炸图结果", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(result, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                            runtime.fakeVideoGuideResult?.let { result ->
                                Spacer(Modifier.height(12.dp))
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("教程播放器", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(result, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            BackendMediaRow(item = item)
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
            onUploadClick = {
                viewModel.showEntrySheet = false
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCaptureClick = {
                Toast.makeText(context, "当前版本暂保留相册上传为主", Toast.LENGTH_SHORT).show()
                viewModel.showEntrySheet = false
            }
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
private fun BackendMediaRow(item: ProjectMediaAssetDto) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(item.fileName ?: "未命名素材", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                if (item.assetType.equals("VIDEO", true)) "视频素材" else "图片素材",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BackendSelectableMediaRow(
    item: ProjectMediaAssetDto,
    selected: Boolean,
    onToggle: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onToggle)
        ) {
            Text(item.fileName ?: "未命名素材", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                (if (item.assetType.equals("VIDEO", true)) "视频素材" else "图片素材") +
                        if (selected) " · 已选择" else " · 未选择",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}