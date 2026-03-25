package com.example.yunjing.ui.merchant.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yunjing.ui.merchant.model.MerchantProjectDto
import com.example.yunjing.ui.merchant.model.ProjectMediaAssetDto
import com.example.yunjing.ui.merchant.model.ProjectModelAssetDto
import com.example.yunjing.ui.merchant.viewmodel.MerchantContentViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api

private enum class MerchantContentPage {
    PROJECT_LIST,
    PROJECT_DETAIL,
    MEDIA_MANAGE,
    REBUILD_SELECT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantContentScreen(
    viewModel: MerchantContentViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isLoading = viewModel.isLoading
    val isUploading = viewModel.isUploading
    val isRebuilding = viewModel.isRebuilding
    val errorMessage = viewModel.errorMessage

    var page by rememberSaveable { mutableStateOf(MerchantContentPage.PROJECT_LIST) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var showUploadSheet by rememberSaveable { mutableStateOf(false) }

    var createProjectName by rememberSaveable { mutableStateOf("") }
    var createProjectDesc by rememberSaveable { mutableStateOf("") }

    val selectedRebuildIds = remember { mutableStateListOf<Long>() }

    val currentProject = viewModel.currentProject()
    val currentProjectDetail = viewModel.currentProjectDetail
    val mediaAssets = viewModel.currentMediaAssets
    val modelAssets = viewModel.currentModelAssets

    fun detectAssetType(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        return if (mimeType.startsWith("video")) "VIDEO" else "IMAGE"
    }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
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
        contract = ActivityResultContracts.GetMultipleContents()
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

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建项目") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = createProjectName,
                        onValueChange = { createProjectName = it },
                        label = { Text("项目名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = createProjectDesc,
                        onValueChange = { createProjectDesc = it },
                        label = { Text("项目说明（选填）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createProject(
                            name = createProjectName,
                            desc = createProjectDesc.ifBlank { null },
                            onSuccess = {
                                createProjectName = ""
                                createProjectDesc = ""
                                showCreateDialog = false
                                page = MerchantContentPage.PROJECT_DETAIL
                            }
                        )
                    }
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showUploadSheet && currentProject != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showUploadSheet = false },
            sheetState = sheetState,
            windowInsets = WindowInsets.navigationBars
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "选择上传方式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "为当前项目上传素材。图片与视频分开选择，更符合后续重建与解析流程。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilledTonalButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showUploadSheet = false
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Collections, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("上传图片")
                }

                FilledTonalButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showUploadSheet = false
                            videoPickerLauncher.launch("video/*")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.PlayCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("上传视频")
                }

                Spacer(
                    modifier = Modifier.height(
                        WindowInsets.ime
                            .getBottom(androidx.compose.ui.platform.LocalDensity.current)
                            .dp
                    )
                )
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (page) {
                MerchantContentPage.PROJECT_LIST -> {
                    ProjectListPage(
                        projects = viewModel.projects,
                        isLoading = isLoading,
                        onCreateProject = { showCreateDialog = true },
                        onOpenProject = { project ->
                            viewModel.selectProject(project.id)
                            selectedRebuildIds.clear()
                            page = MerchantContentPage.PROJECT_DETAIL
                        }
                    )
                }

                MerchantContentPage.PROJECT_DETAIL -> {
                    if (currentProject == null) {
                        EmptyStateCard(
                            title = "暂无已选项目",
                            desc = "请先从项目列表中选择一个项目。"
                        ) {
                            page = MerchantContentPage.PROJECT_LIST
                        }
                    } else {
                        ProjectWorkbenchPage(
                            project = currentProject,
                            projectDesc = currentProject.projectDesc,
                            mediaCount = mediaAssets.size,
                            modelCount = modelAssets.size,
                            isUploading = isUploading,
                            isRebuilding = isRebuilding,
                            selectedRebuildCount = selectedRebuildIds.size,
                            onBack = {
                                selectedRebuildIds.clear()
                                page = MerchantContentPage.PROJECT_LIST
                            },
                            onOpenMediaFolder = {
                                page = MerchantContentPage.MEDIA_MANAGE
                            },
                            onOpenRebuildSelector = {
                                page = MerchantContentPage.REBUILD_SELECT
                            },
                            onUpload = {
                                showUploadSheet = true
                            },
                            onStartRebuild = {
                                if (selectedRebuildIds.isEmpty()) {
                                    Toast.makeText(context, "请先选择待重建的图片素材", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.rebuildProject(
                                        projectId = currentProject.id,
                                        sourceAssetIds = selectedRebuildIds.toList(),
                                        onSuccess = {
                                            Toast.makeText(context, "重建任务已提交", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            modelAssets = modelAssets,
                            detailText = currentProjectDetail?.project?.projectDesc
                        )
                    }
                }

                MerchantContentPage.MEDIA_MANAGE -> {
                    if (currentProject == null) {
                        EmptyStateCard(
                            title = "项目信息丢失",
                            desc = "请返回项目列表重新进入。"
                        ) {
                            page = MerchantContentPage.PROJECT_LIST
                        }
                    } else {
                        MediaManagePage(
                            projectName = currentProject.projectName,
                            mediaAssets = mediaAssets,
                            isUploading = isUploading,
                            onBack = { page = MerchantContentPage.PROJECT_DETAIL },
                            onAddMedia = { showUploadSheet = true }
                        )
                    }
                }

                MerchantContentPage.REBUILD_SELECT -> {
                    if (currentProject == null) {
                        EmptyStateCard(
                            title = "项目信息丢失",
                            desc = "请返回项目列表重新进入。"
                        ) {
                            page = MerchantContentPage.PROJECT_LIST
                        }
                    } else {
                        RebuildSelectPage(
                            projectName = currentProject.projectName,
                            mediaAssets = mediaAssets,
                            selectedIds = selectedRebuildIds,
                            isRebuilding = isRebuilding,
                            onBack = { page = MerchantContentPage.PROJECT_DETAIL },
                            onToggle = { assetId ->
                                if (selectedRebuildIds.contains(assetId)) {
                                    selectedRebuildIds.remove(assetId)
                                } else {
                                    selectedRebuildIds.add(assetId)
                                }
                            },
                            onConfirm = {
                                if (selectedRebuildIds.isEmpty()) {
                                    Toast.makeText(context, "请至少选择一张图片", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.rebuildProject(
                                        projectId = currentProject.id,
                                        sourceAssetIds = selectedRebuildIds.toList(),
                                        onSuccess = {
                                            Toast.makeText(context, "重建完成", Toast.LENGTH_SHORT).show()
                                            page = MerchantContentPage.PROJECT_DETAIL
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }

            if (isLoading && viewModel.projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ProjectListPage(
    projects: List<MerchantProjectDto>,
    isLoading: Boolean,
    onCreateProject: () -> Unit,
    onOpenProject: (MerchantProjectDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroHeaderCard(
                title = "内容库",
                subtitle = "以项目为单位组织上传、重建、解析与发布流程，恢复旧版工作台式管理体验。"
            )
        }

        item {
            SoftPanelCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "项目管理",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "先创建项目，再进入项目内执行素材上传、重建与解析。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(onClick = onCreateProject) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("新建项目")
                    }
                }
            }
        }

        if (projects.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (isLoading) "项目加载中" else "暂无项目",
                    desc = if (isLoading) "正在从后端同步项目数据。" else "点击右上方“新建项目”开始创建内容生产流程。"
                )
            }
        } else {
            items(projects, key = { it.id }) { project ->
                ProjectListItemCard(
                    project = project,
                    onClick = { onOpenProject(project) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun ProjectWorkbenchPage(
    project: MerchantProjectDto,
    projectDesc: String?,
    detailText: String?,
    mediaCount: Int,
    modelCount: Int,
    isUploading: Boolean,
    isRebuilding: Boolean,
    selectedRebuildCount: Int,
    onBack: () -> Unit,
    onOpenMediaFolder: () -> Unit,
    onOpenRebuildSelector: () -> Unit,
    onUpload: () -> Unit,
    onStartRebuild: () -> Unit,
    modelAssets: List<ProjectModelAssetDto>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("返回项目列表")
                }
            }
        }

        item {
            HeroHeaderCard(
                title = project.projectName,
                subtitle = projectDesc ?: detailText ?: "该项目已进入内容工作台，可继续进行素材管理、模型重建与解析发布。"
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(label = "素材", value = mediaCount.toString())
                StatChip(label = "模型", value = modelCount.toString())
                StatChip(label = "已选重建", value = selectedRebuildCount.toString())
            }
        }

        item {
            WorkbenchSectionCard(
                title = "素材管理",
                desc = "按旧版流程保留“素材文件夹”入口，项目内素材统一管理。"
            ) {
                FolderEntryCard(
                    icon = Icons.Outlined.FolderOpen,
                    title = "素材文件夹",
                    desc = "进入后可查看当前项目已上传的图片与视频素材。",
                    actionText = if (isUploading) "上传中..." else "进入文件夹",
                    onClick = onOpenMediaFolder
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onUpload,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("添加素材")
                }
            }
        }

        item {
            WorkbenchSectionCard(
                title = "重建",
                desc = "仅支持从已上传图片中勾选素材进行重建，保留旧版工作台操作路径。"
            ) {
                FolderEntryCard(
                    icon = Icons.Outlined.AutoAwesome,
                    title = "选择重建素材",
                    desc = "进入选择页后仅展示图片素材，可勾选后发起模型重建。",
                    actionText = "去选择",
                    onClick = onOpenRebuildSelector
                )
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onStartRebuild,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isRebuilding
                ) {
                    Icon(Icons.Outlined.ViewInAr, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRebuilding) "重建中..." else "开始重建")
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "模型输出",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (modelAssets.isEmpty()) {
                    InlineHintCard("当前暂无模型输出。完成重建后，模型文件会在这里展示。")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        modelAssets.forEach { model ->
                            ModelResultCard(model = model)
                        }
                    }
                }
            }
        }

        item {
            WorkbenchSectionCard(
                title = "解析",
                desc = "保留旧版解析工作台外观；当前页面先展示入口和说明，后续可再接入解析后端接口。"
            ) {
                ParsePlaceholderCard(
                    title = "说明书解析",
                    desc = "用于生成分步拆解说明、结构信息与图文指引。",
                    icon = Icons.Outlined.Description
                )
                Spacer(modifier = Modifier.height(10.dp))
                ParsePlaceholderCard(
                    title = "视频教程解析",
                    desc = "用于抽取讲解片段、操作步骤与关键动作信息。",
                    icon = Icons.Outlined.PlayCircleOutline
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun MediaManagePage(
    projectName: String,
    mediaAssets: List<ProjectMediaAssetDto>,
    isUploading: Boolean,
    onBack: () -> Unit,
    onAddMedia: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("返回工作台")
                }
            }
        }

        item {
            HeroHeaderCard(
                title = "素材文件夹",
                subtitle = "项目：$projectName"
            )
        }

        item {
            SoftPanelCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "素材管理",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "图片与视频统一归档，供后续重建和解析流程选择。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onAddMedia) {
                        Text(if (isUploading) "上传中..." else "添加素材")
                    }
                }
            }
        }

        if (mediaAssets.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "暂无素材",
                    desc = "当前项目还没有上传任何图片或视频。"
                )
            }
        } else {
            items(mediaAssets, key = { it.id }) { asset ->
                MediaAssetCard(asset = asset)
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun RebuildSelectPage(
    projectName: String,
    mediaAssets: List<ProjectMediaAssetDto>,
    selectedIds: List<Long>,
    isRebuilding: Boolean,
    onBack: () -> Unit,
    onToggle: (Long) -> Unit,
    onConfirm: () -> Unit
) {
    val imageAssets = mediaAssets.filter { it.assetType.equals("IMAGE", ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("返回工作台")
                }
            }
        }

        item {
            HeroHeaderCard(
                title = "重建素材选择",
                subtitle = "项目：$projectName，仅展示图片素材。"
            )
        }

        item {
            SoftPanelCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "已选择 ${selectedIds.size} 项",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "建议选择同一对象、同一场景下的多角度图片，提高重建质量。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isRebuilding
                    ) {
                        Text(if (isRebuilding) "重建中..." else "确认重建")
                    }
                }
            }
        }

        if (imageAssets.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "暂无可重建图片",
                    desc = "请先在素材文件夹中上传图片素材，视频素材不可直接用于当前重建流程。"
                )
            }
        } else {
            items(imageAssets, key = { it.id }) { asset ->
                RebuildSelectableAssetCard(
                    asset = asset,
                    checked = selectedIds.contains(asset.id),
                    onToggle = { onToggle(asset.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun HeroHeaderCard(
    title: String,
    subtitle: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SoftPanelCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun WorkbenchSectionCard(
    title: String,
    desc: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ProjectListItemCard(
    project: MerchantProjectDto,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.projectDesc ?: "暂无项目说明",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AssistChip(
                onClick = onClick,
                label = { Text("进入") },
                colors = AssistChipDefaults.assistChipColors()
            )
        }
    }
}

@Composable
private fun FolderEntryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    actionText: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ParsePlaceholderCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            InlineHintCard("当前仅恢复工作台外观，解析后端接口接入后可继续扩展。")
        }
    }
}

@Composable
private fun MediaAssetCard(
    asset: ProjectMediaAssetDto
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (asset.assetType.equals("VIDEO", true)) {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (asset.assetType.equals("VIDEO", true)) {
                            Icons.Outlined.PlayCircleOutline
                        } else {
                            Icons.Outlined.Collections
                        },
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.fileName ?: "未命名素材",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (asset.assetType.equals("VIDEO", true)) "视频素材" else "图片素材",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RebuildSelectableAssetCard(
    asset: ProjectMediaAssetDto,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = { onToggle() }
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (checked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (checked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.fileName ?: "未命名图片",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "图片素材 · 可用于重建",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModelResultCard(
    model: ProjectModelAssetDto
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = model.modelName ?: "模型文件",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "模型已生成，可在后续接入模型预览器或解析流程后继续使用。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InlineHintCard(
    text: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    desc: String,
    action: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (action != null) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(onClick = action) {
                    Text("返回")
                }
            }
        }
    }
}