package com.example.yunjing.ui.merchant.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yunjing.ui.merchant.model.ContentPageState
import com.example.yunjing.ui.merchant.model.MerchantProjectDetailDto
import com.example.yunjing.ui.merchant.model.MerchantProjectDto
import com.example.yunjing.ui.merchant.model.ParseMode
import com.example.yunjing.ui.merchant.model.ProjectMediaAssetDto
import com.example.yunjing.ui.merchant.model.ProjectModelAssetDto
import com.example.yunjing.ui.merchant.repository.MerchantContentRepository
import kotlinx.coroutines.launch

enum class LibraryStage {
    PENDING_UPLOAD,
    PENDING_REBUILD,
    PENDING_PARSE,
    READY,
    PUBLISHED
}

class MerchantContentViewModel(
    private val repository: MerchantContentRepository
) : ViewModel() {

    companion object {
        private const val DEMO_USER_ID = 1L
    }

    data class ProjectRuntimeState(
        val publishStatus: String = "待处理",
        val selectedRebuildAssetIds: List<Long> = emptyList(),
        val selectedParseSourceAssetIds: List<Long> = emptyList(),
        val selectedParseModelIds: List<Long> = emptyList(),
        val parseMode: ParseMode = ParseMode.EXPLODED_GUIDE,
        val isFakeRebuilding: Boolean = false,
        val rebuildProgress: Float = 0f,
        val fakeRebuildResult: String? = null,
        val isFakeParsing: Boolean = false,
        val parseProgress: Float = 0f,
        val fakeExplodedGuideResult: String? = null,
        val fakeExplodedImageUrl: String? = null,
        val fakeVideoGuideResult: String? = null,
    )

    val projects = mutableStateListOf<MerchantProjectDto>()
    val currentMediaAssets = mutableStateListOf<ProjectMediaAssetDto>()
    val currentModelAssets = mutableStateListOf<ProjectModelAssetDto>()

    private val localPreviewUriMap = mutableStateMapOf<Long, Uri>()

    private val pendingLocalPreviewQueue = mutableStateMapOf<Long, MutableList<Pair<String, Uri>>>()

    private fun enqueuePendingLocalPreview(projectId: Long, assetType: String, uri: Uri) {
        val queue = pendingLocalPreviewQueue.getOrPut(projectId) { mutableListOf() }
        queue.add(assetType.uppercase() to uri)
    }

    private fun reconcileLocalPreviewUris(
        projectId: Long,
        mediaAssets: List<ProjectMediaAssetDto>
    ) {
        val queue = pendingLocalPreviewQueue[projectId] ?: return
        if (queue.isEmpty()) return

        val unboundAssets = mediaAssets
            .sortedByDescending { it.id }
            .filter { localPreviewUriMap[it.id] == null }

        val iterator = queue.iterator()
        val remaining = mutableListOf<Pair<String, Uri>>()

        while (iterator.hasNext()) {
            val pending = iterator.next()
            val assetType = pending.first
            val uri = pending.second

            val target = unboundAssets.firstOrNull { asset ->
                asset.assetType.equals(assetType, ignoreCase = true) &&
                        localPreviewUriMap[asset.id] == null
            }

            if (target != null) {
                localPreviewUriMap[target.id] = uri
            } else {
                remaining.add(pending)
            }
        }

        if (remaining.isEmpty()) {
            pendingLocalPreviewQueue.remove(projectId)
        } else {
            pendingLocalPreviewQueue[projectId] = remaining.toMutableList()
        }
    }

    fun bindLocalPreviewUri(assetId: Long, uri: Uri) {
        localPreviewUriMap[assetId] = uri
    }

    fun localPreviewUriOf(assetId: Long): Uri? = localPreviewUriMap[assetId]

    fun removeLocalPreviewUri(assetId: Long) {
        localPreviewUriMap.remove(assetId)
    }

    private val runtimeStates = mutableStateMapOf<Long, ProjectRuntimeState>()

    var selectedProjectId by mutableStateOf<Long?>(null)
    var currentProjectDetail by mutableStateOf<MerchantProjectDetailDto?>(null)
        private set

    var pageState by mutableStateOf(ContentPageState.PROJECT_LIST)
    var showEntrySheet by mutableStateOf(false)

    var isLoading by mutableStateOf(false)
        private set

    var isUploading by mutableStateOf(false)
        private set

    var isRebuilding by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var showCreateProjectDialog by mutableStateOf(false)
    var showRenameProjectDialog by mutableStateOf(false)
    var showDeleteProjectDialog by mutableStateOf(false)

    init {
        loadProjects()
    }

    fun loadProjects(userId: Long = DEMO_USER_ID) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.listProjects(userId)
                .onSuccess { list ->
                    projects.clear()
                    projects.addAll(list)

                    list.forEach { ensureRuntimeState(it.id) }

                    val currentSelectedId = selectedProjectId
                    if (currentSelectedId == null && list.isNotEmpty()) {
                        selectProject(list.first().id)
                    } else if (currentSelectedId != null && list.none { it.id == currentSelectedId }) {
                        clearSelectedProject()
                    }
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "加载项目失败"
                }

            isLoading = false
        }
    }

    fun createProject(
        name: String,
        desc: String? = null,
        userId: Long = DEMO_USER_ID,
        onSuccess: (() -> Unit)? = null
    ) {
        val trimmed = name.trim()

        if (trimmed.isEmpty()) {
            errorMessage = "项目名称不能为空"
            return
        }

        if (projects.any { it.projectName.trim() == trimmed }) {
            errorMessage = "已存在同名项目，请更换名称"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.createProject(
                userId = userId,
                projectName = trimmed,
                projectDesc = desc?.trim()?.takeIf { it.isNotBlank() }
            ).onSuccess { project ->
                ensureRuntimeState(project.id)
                loadProjects(userId)
                selectedProjectId = project.id
                pageState = ContentPageState.PROJECT_DETAIL
                loadProjectDetail(project.id)
                showCreateProjectDialog = false
                onSuccess?.invoke()
            }.onFailure { error ->
                errorMessage = error.message ?: "创建项目失败"
            }

            isLoading = false
        }
    }

    fun renameProject(
        projectId: Long,
        newName: String,
        onSuccess: (() -> Unit)? = null
    ) {
        val trimmed = newName.trim()

        if (trimmed.isEmpty()) {
            errorMessage = "项目名称不能为空"
            return
        }

        val currentName = projects.firstOrNull { it.id == projectId }?.projectName?.trim()
        if (currentName != null && currentName == trimmed) {
            showRenameProjectDialog = false
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.renameProject(projectId, trimmed)
                .onSuccess {
                    val index = projects.indexOfFirst { it.id == projectId }
                    if (index >= 0) {
                        projects[index] = projects[index].copy(projectName = trimmed)
                    }

                    currentProjectDetail = currentProjectDetail?.let { detail ->
                        if (detail.project.id == projectId) {
                            detail.copy(project = detail.project.copy(projectName = trimmed))
                        } else {
                            detail
                        }
                    }

                    showRenameProjectDialog = false
                    onSuccess?.invoke()
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "重命名项目失败，请稍后再试"
                }

            isLoading = false
        }
    }

    fun deleteProject(
        projectId: Long,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // 假设 repository 已补充 deleteProject(projectId)
            repository.deleteProject(projectId)
                .onSuccess {
                    projects.removeAll { it.id == projectId }
                    removeRuntimeState(projectId)
                    showDeleteProjectDialog = false

                    if (selectedProjectId == projectId) {
                        clearSelectedProject()
                        if (projects.isNotEmpty()) {
                            selectProject(projects.first().id)
                        }
                        pageState = ContentPageState.PROJECT_LIST
                    }

                    onSuccess?.invoke()
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "删除项目失败"
                }

            isLoading = false
        }
    }

    fun selectProject(projectId: Long) {
        selectedProjectId = projectId
        ensureRuntimeState(projectId)
        loadProjectDetail(projectId)
    }

    fun clearSelectedProject() {
        selectedProjectId = null
        currentProjectDetail = null
        currentMediaAssets.clear()
        currentModelAssets.clear()
    }

    fun deriveStage(projectId: Long): LibraryStage {
        val runtime = runtimeStateOf(projectId)
        val imageCount = currentMediaAssets.count { it.assetType.equals("IMAGE", true) }
        val modelCount = currentModelAssets.size

        return when {
            runtime.publishStatus == "已发布" -> LibraryStage.PUBLISHED
            runtime.fakeExplodedGuideResult != null || runtime.fakeVideoGuideResult != null -> LibraryStage.READY
            imageCount == 0 -> LibraryStage.PENDING_UPLOAD
            modelCount == 0 && runtime.fakeRebuildResult == null -> LibraryStage.PENDING_REBUILD
            else -> LibraryStage.PENDING_PARSE
        }
    }

    fun stageLabel(projectId: Long): String {
        return when (deriveStage(projectId)) {
            LibraryStage.PENDING_UPLOAD -> "待上传"
            LibraryStage.PENDING_REBUILD -> "待重建"
            LibraryStage.PENDING_PARSE -> "待解析"
            LibraryStage.READY -> "待发布"
            LibraryStage.PUBLISHED -> "已发布"
        }
    }

    fun refreshCurrentProject() {
        selectedProjectId?.let { loadProjectDetail(it) }
    }

    fun loadProjectDetail(projectId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getProjectDetail(projectId)
                .onSuccess { detail ->
                    currentProjectDetail = detail
                    ensureRuntimeState(detail.project.id)

                    currentMediaAssets.clear()
                    currentMediaAssets.addAll(detail.mediaAssets)

                    currentModelAssets.clear()
                    currentModelAssets.addAll(detail.modelAssets)
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "获取项目详情失败"
                }

            isLoading = false
        }
    }

    fun uploadMedia(
        context: Context,
        projectId: Long,
        assetType: String,
        uri: Uri,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            isUploading = true
            errorMessage = null

            enqueuePendingLocalPreview(projectId, assetType, uri)

            repository.uploadMedia(
                context = context,
                projectId = projectId,
                assetType = assetType,
                uri = uri
            ).onSuccess {
                repository.getProjectDetail(projectId)
                    .onSuccess { detail ->
                        currentProjectDetail = detail
                        ensureRuntimeState(detail.project.id)

                        currentMediaAssets.clear()
                        currentMediaAssets.addAll(detail.mediaAssets)

                        currentModelAssets.clear()
                        currentModelAssets.addAll(detail.modelAssets)

                        reconcileLocalPreviewUris(projectId, detail.mediaAssets)

                        onSuccess?.invoke()
                    }
                    .onFailure { error ->
                        errorMessage = error.message ?: "刷新项目详情失败"
                    }
            }.onFailure { error ->
                errorMessage = error.message ?: "素材上传失败"
            }

            isUploading = false
        }
    }

    fun deleteMedia(
        projectId: Long,
        mediaId: Long,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.deleteMedia(projectId, mediaId)
                .onSuccess {
                    currentMediaAssets.removeAll { it.id == mediaId }
                    removeLocalPreviewUri(mediaId)

                    currentProjectDetail = currentProjectDetail?.let { detail ->
                        if (detail.project.id == projectId) {
                            detail.copy(
                                mediaAssets = detail.mediaAssets.filterNot { it.id == mediaId }
                            )
                        } else {
                            detail
                        }
                    }

                    runtimeStates[projectId]?.let { state ->
                        runtimeStates[projectId] = state.copy(
                            selectedRebuildAssetIds = state.selectedRebuildAssetIds.filterNot { it == mediaId },
                            selectedParseSourceAssetIds = state.selectedParseSourceAssetIds.filterNot { it == mediaId }
                        )
                    }

                    onSuccess?.invoke()
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "删除素材失败"
                }

            isLoading = false
        }
    }

    fun loadModels(projectId: Long) {
        viewModelScope.launch {
            errorMessage = null

            repository.listModels(projectId)
                .onSuccess { models ->
                    currentModelAssets.clear()
                    currentModelAssets.addAll(models)
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "加载模型失败"
                }
        }
    }

    fun currentProject(): MerchantProjectDto? {
        val id = selectedProjectId ?: return null
        return projects.firstOrNull { it.id == id }
    }

    fun ensureRuntimeState(projectId: Long) {
        if (runtimeStates[projectId] == null) {
            runtimeStates[projectId] = ProjectRuntimeState()
        }
    }

    fun runtimeStateOf(projectId: Long): ProjectRuntimeState {
        ensureRuntimeState(projectId)
        return runtimeStates.getValue(projectId)
    }

    fun updateRuntimeState(
        projectId: Long,
        transform: (ProjectRuntimeState) -> ProjectRuntimeState
    ) {
        val current = runtimeStateOf(projectId)
        runtimeStates[projectId] = transform(current)
    }

    fun removeRuntimeState(projectId: Long) {
        runtimeStates.remove(projectId)
    }

    fun toggleFakeRebuildAsset(projectId: Long, assetId: Long) {
        updateRuntimeState(projectId) { state ->
            val next = state.selectedRebuildAssetIds.toMutableList()
            if (next.contains(assetId)) next.remove(assetId) else next.add(assetId)
            state.copy(selectedRebuildAssetIds = next)
        }
    }

    fun clearFakeRebuildSelection(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(selectedRebuildAssetIds = emptyList())
        }
    }

    fun startFakeRebuild(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(
                isFakeRebuilding = true,
                rebuildProgress = 0f,
                fakeRebuildResult = null
            )
        }
    }

    fun finishFakeRebuild(projectId: Long) {
        updateRuntimeState(projectId) { state ->
            state.copy(
                isFakeRebuilding = false,
                rebuildProgress = 1f,
                fakeRebuildResult = "已完成模型重建，模型文件已归档到模型文件夹。",
                publishStatus = if (state.publishStatus == "已发布") "已发布" else "待发布"
            )
        }

        loadModels(projectId)
    }

    fun toggleFakeParseMode(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(
                parseMode = if (it.parseMode == ParseMode.EXPLODED_GUIDE) {
                    ParseMode.VIDEO_GUIDE
                } else {
                    ParseMode.EXPLODED_GUIDE
                },
                selectedParseSourceAssetIds = emptyList(),
                selectedParseModelIds = emptyList(),
                fakeExplodedGuideResult = null,
                fakeExplodedImageUrl = null,
                fakeVideoGuideResult = null,
                isFakeParsing = false,
                parseProgress = 0f
            )
        }
    }

    fun toggleFakeParseModel(projectId: Long, modelId: Long) {
        updateRuntimeState(projectId) { state ->
            val next = state.selectedParseModelIds.toMutableList()
            if (next.contains(modelId)) next.remove(modelId) else next.add(modelId)
            state.copy(selectedParseModelIds = next)
        }
    }

    fun toggleFakeParseAsset(projectId: Long, assetId: Long) {
        updateRuntimeState(projectId) { state ->
            val next = state.selectedParseSourceAssetIds.toMutableList()
            if (next.contains(assetId)) next.remove(assetId) else next.add(assetId)
            state.copy(selectedParseSourceAssetIds = next)
        }
    }

    fun clearFakeParseSelection(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(
                selectedParseSourceAssetIds = emptyList(),
                selectedParseModelIds = emptyList(),
                fakeExplodedGuideResult = null,
                fakeExplodedImageUrl = null,
                fakeVideoGuideResult = null,
                isFakeParsing = false,
                parseProgress = 0f
            )
        }
    }

    fun startFakeParse(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(
                isFakeParsing = true,
                parseProgress = 0f,
                fakeExplodedGuideResult = null,
                fakeVideoGuideResult = null
            )
        }
    }

    fun finishFakeParse(projectId: Long) {
        updateRuntimeState(projectId) { state ->
            if (state.parseMode == ParseMode.EXPLODED_GUIDE) {
                state.copy(
                    isFakeParsing = false,
                    parseProgress = 1f,
                    fakeExplodedGuideResult = "已生成爆炸图说明结果，可继续查看或发布。",
                    fakeExplodedImageUrl = "file:///android_asset/photo/exploded_demo.png",
                    fakeVideoGuideResult = null,
                    publishStatus = if (state.publishStatus == "已发布") "已发布" else "待发布"
                )
            } else {
                state.copy(
                    isFakeParsing = false,
                    parseProgress = 1f,
                    fakeExplodedGuideResult = null,
                    fakeExplodedImageUrl = null,
                    fakeVideoGuideResult = "已生成教程播放器入口结果，可继续打开",
                    publishStatus = if (state.publishStatus == "已发布") "已发布" else "待发布"
                )
            }
        }
    }

    fun markFakePublish(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(publishStatus = "已发布")
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
