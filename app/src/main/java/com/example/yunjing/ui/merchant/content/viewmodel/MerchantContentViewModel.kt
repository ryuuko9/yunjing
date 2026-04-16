package com.example.yunjing.ui.merchant.content.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yunjing.ui.merchant.content.model.ContentPageState
import com.example.yunjing.ui.merchant.content.model.ParseMode
import com.example.yunjing.ui.merchant.content.repository.MerchantContentRepository
import com.example.yunjing.ui.merchant.model.MerchantProjectDetailDto
import com.example.yunjing.ui.merchant.model.MerchantProjectDto
import com.example.yunjing.ui.merchant.model.ProjectMediaAssetDto
import com.example.yunjing.ui.merchant.model.ProjectModelAssetDto
import com.example.yunjing.ui.merchant.model.toMerchantProjectDto
import kotlinx.coroutines.launch

/**
 * 本文件负责维护 merchant 内容库页面的共享状态、流程动作与页面切换。
 */

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

    data class ProjectRuntimeState(
        val publishStatus: String = "待处理",
        val selectedRebuildAssetIds: List<Long> = emptyList(),
        val selectedParseSourceAssetIds: List<Long> = emptyList(),
        val selectedParseModelIds: List<Long> = emptyList(),
        val parseMode: ParseMode = ParseMode.EXPLODED_GUIDE,
        val isRealRebuilding: Boolean = false,
        val rebuildProgress: Float = 0f,
        val realRebuildResult: String? = null,
        val realParsing: Boolean = false,
        val parseProgress: Float = 0f,
    )

    var projects = mutableStateListOf<MerchantProjectDto>()
    val currentMediaAssets = mutableStateListOf<ProjectMediaAssetDto>()
    val currentModelAssets = mutableStateListOf<ProjectModelAssetDto>()
    private val projectMediaCountMap = mutableStateMapOf<Long, Int>()
    private val projectModelCountMap = mutableStateMapOf<Long, Int>()


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

    fun openProjectFromDashboard(projectId: Long) {
        selectedProjectId = projectId
        pageState = ContentPageState.PROJECT_DETAIL
        ensureRuntimeState(projectId)
        loadProjectDetail(projectId)
    }

    fun localPreviewUriOf(assetId: Long): Uri? = localPreviewUriMap[assetId]

    fun removeLocalPreviewUri(assetId: Long) {
        localPreviewUriMap.remove(assetId)
    }

    private val runtimeStates = mutableStateMapOf<Long, ProjectRuntimeState>()

    private fun syncProjectAssetCounts(
        projectId: Long,
        mediaAssets: List<ProjectMediaAssetDto>,
        modelAssets: List<ProjectModelAssetDto>
    ) {
        projectMediaCountMap[projectId] = mediaAssets.size
        projectModelCountMap[projectId] = modelAssets.size
    }

    private fun clearProjectAssetCounts(projectId: Long) {
        projectMediaCountMap.remove(projectId)
        projectModelCountMap.remove(projectId)
    }

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

    var previewModel by mutableStateOf<ProjectModelAssetDto?>(null)
        private set

    private var currentUserId by mutableStateOf<Long?>(null)

    fun openModelPreview(model: ProjectModelAssetDto) {
        previewModel = model
        pageState = ContentPageState.MODEL_PREVIEW
    }

    fun closeModelPreview() {
        previewModel = null
        pageState = ContentPageState.MODEL_FOLDER_MANAGE
    }

    fun bindUser(userId: Long?) {
        if (currentUserId == userId) return

        currentUserId = userId
        resetUserScopedState()

        if (userId != null) {
            loadProjects(userId)
        }
    }

    fun loadProjects(userId: Long? = currentUserId) {
        val resolvedUserId = userId ?: run {
            errorMessage = "当前商家账号缺少用户 ID，请重新登录"
            return
        }

        currentUserId = resolvedUserId
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.listProjects(resolvedUserId)
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
        userId: Long? = currentUserId,
        onSuccess: (() -> Unit)? = null
    ) {
        val resolvedUserId = userId ?: run {
            errorMessage = "当前商家账号缺少用户 ID，请重新登录"
            return
        }

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
                userId = resolvedUserId,
                projectName = trimmed,
                projectDesc = desc?.trim()?.takeIf { it.isNotBlank() }
            ).onSuccess { project ->
                ensureRuntimeState(project.id)
                loadProjects(resolvedUserId)
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
        val resolvedUserId = requireCurrentUserId() ?: return
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

            repository.renameProject(resolvedUserId, projectId, trimmed)
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
        val resolvedUserId = requireCurrentUserId() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.deleteProject(resolvedUserId, projectId)
                .onSuccess {
                    projects.removeAll { it.id == projectId }
                    removeRuntimeState(projectId)
                    showDeleteProjectDialog = false
                    clearProjectAssetCounts(projectId)
                    removeRuntimeState(projectId)

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
        val imageCount = projectMediaCountMap[projectId] ?: 0
        val modelCount = projectModelCountMap[projectId] ?: 0
        val project = projects.firstOrNull { it.id == projectId }

        return when {
            project?.publishStatus == "PUBLISHED" || runtime.publishStatus == "已发布" -> LibraryStage.PUBLISHED

            !project?.explodedImageUrl.isNullOrBlank() ||
                    !project?.tutorialVideoUrl.isNullOrBlank() -> LibraryStage.READY

            runtime.realParsing -> LibraryStage.PENDING_PARSE

            runtime.isRealRebuilding -> LibraryStage.PENDING_REBUILD

            imageCount == 0 -> LibraryStage.PENDING_UPLOAD

            modelCount == 0 && runtime.realRebuildResult == null -> LibraryStage.PENDING_REBUILD

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

    fun loadProjectDetail(projectId: Long) {
        val resolvedUserId = requireCurrentUserId() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getProjectDetail(resolvedUserId, projectId)
                .onSuccess { detail ->
                    currentProjectDetail = detail
                    ensureRuntimeState(detail.project.id)

                    currentMediaAssets.clear()
                    currentMediaAssets.addAll(detail.mediaAssets)

                    currentModelAssets.clear()
                    currentModelAssets.addAll(detail.modelAssets)

                    syncProjectAssetCounts(
                        projectId = detail.project.id,
                        mediaAssets = detail.mediaAssets,
                        modelAssets = detail.modelAssets
                    )

                    updateProjectInList(detail.project.id) { detail.project }
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
        val resolvedUserId = requireCurrentUserId() ?: return
        viewModelScope.launch {
            isUploading = true
            errorMessage = null

            enqueuePendingLocalPreview(projectId, assetType, uri)

            repository.uploadMedia(
                context = context,
                userId = resolvedUserId,
                projectId = projectId,
                assetType = assetType,
                uri = uri
            ).onSuccess {
                repository.getProjectDetail(resolvedUserId, projectId)
                    .onSuccess { detail ->
                        currentProjectDetail = detail
                        ensureRuntimeState(detail.project.id)

                        currentMediaAssets.clear()
                        currentMediaAssets.addAll(detail.mediaAssets)

                        currentModelAssets.clear()
                        currentModelAssets.addAll(detail.modelAssets)

                        syncProjectAssetCounts(
                            projectId = detail.project.id,
                            mediaAssets = detail.mediaAssets,
                            modelAssets = detail.modelAssets
                        )

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
        val resolvedUserId = requireCurrentUserId() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.deleteMedia(resolvedUserId, projectId, mediaId)
                .onSuccess {
                    currentMediaAssets.removeAll { it.id == mediaId }
                    removeLocalPreviewUri(mediaId)
                    projectMediaCountMap[projectId] = currentMediaAssets.count {
                        it.assetType.equals("IMAGE", true)
                    }

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
        val resolvedUserId = requireCurrentUserId() ?: return
        viewModelScope.launch {
            errorMessage = null

            repository.listModels(resolvedUserId, projectId)
                .onSuccess { models ->
                    currentModelAssets.clear()
                    currentModelAssets.addAll(models)
                    projectModelCountMap[projectId] = models.size
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

    fun toggleRebuildAsset(projectId: Long, assetId: Long) {
        updateRuntimeState(projectId) { state ->
            val next = state.selectedRebuildAssetIds.toMutableList()
            if (next.contains(assetId)) next.remove(assetId) else next.add(assetId)
            state.copy(selectedRebuildAssetIds = next)
        }
    }

    fun clearRebuildSelection(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(selectedRebuildAssetIds = emptyList())
        }
    }

    fun startRebuild(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(
                isRealRebuilding = true,
                rebuildProgress = 0f,
                realRebuildResult = null
            )
        }
    }

    fun finishRebuild(projectId: Long) {
        val resolvedUserId = requireCurrentUserId() ?: return
        val selectedAssetIds = runtimeStateOf(projectId).selectedRebuildAssetIds

        if (selectedAssetIds.isEmpty()) {
            updateRuntimeState(projectId) { state ->
                state.copy(
                    isRealRebuilding = false,
                    rebuildProgress = 0f,
                    realRebuildResult = null
                )
            }
            errorMessage = "请先选择重建素材"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.rebuildProject(
                userId = resolvedUserId,
                projectId = projectId,
                sourceAssetIds = selectedAssetIds
            ).onSuccess { models ->
                currentModelAssets.clear()
                currentModelAssets.addAll(models)
                projectModelCountMap[projectId] = models.size

                currentProjectDetail = currentProjectDetail?.let { detail ->
                    if (detail.project.id == projectId) {
                        detail.copy(modelAssets = models)
                    } else {
                        detail
                    }
                }

                updateRuntimeState(projectId) { state ->
                    state.copy(
                        isRealRebuilding = false,
                        rebuildProgress = 1f,
                        realRebuildResult = if (models.isEmpty()) {
                            "重建已完成，但后端暂未返回模型文件。"
                        } else {
                            "已完成模型重建，模型文件已归档到模型文件夹。"
                        }
                    )
                }

                updateProjectInList(projectId) { project ->
                    project.copy(
                        rebuildStatus = "COMPLETED",
                        hasRebuildOutput = if (models.isNotEmpty()) 1 else project.hasRebuildOutput,
                        status = "REBUILT"
                    )
                }

                // 刷新一次详情和模型列表
                loadProjectDetail(projectId)
                loadModels(projectId)
            }.onFailure { error ->
                updateRuntimeState(projectId) { state ->
                    state.copy(
                        isRealRebuilding = false,
                        rebuildProgress = 0f,
                        realRebuildResult = null
                    )
                }
                errorMessage = error.message ?: "模型重建失败"
            }

            isLoading = false
        }
    }

    private fun updateProjectInList(
        projectId: Long,
        transform: (MerchantProjectDto) -> MerchantProjectDto
    ) {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index >= 0) {
            projects[index] = transform(projects[index])
        }
    }

    fun toggleParseMode(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(
                parseMode = if (it.parseMode == ParseMode.EXPLODED_GUIDE) {
                    ParseMode.VIDEO_GUIDE
                } else {
                    ParseMode.EXPLODED_GUIDE
                },
                selectedParseSourceAssetIds = emptyList(),
                selectedParseModelIds = emptyList(),
                realParsing = false,
                parseProgress = 0f
            )
        }
    }

    fun toggleParseModel(projectId: Long, modelId: Long) {
        updateRuntimeState(projectId) { state ->
            val next = state.selectedParseModelIds.toMutableList()
            if (next.contains(modelId)) next.remove(modelId) else next.add(modelId)
            state.copy(selectedParseModelIds = next)
        }
    }

    fun toggleParseAsset(projectId: Long, assetId: Long) {
        updateRuntimeState(projectId) { state ->
            val next = state.selectedParseSourceAssetIds.toMutableList()
            if (next.contains(assetId)) next.remove(assetId) else next.add(assetId)
            state.copy(selectedParseSourceAssetIds = next)
        }
    }

    fun clearParseSelection(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(
                selectedParseSourceAssetIds = emptyList(),
                selectedParseModelIds = emptyList(),
                realParsing = false,
                parseProgress = 0f
            )
        }
    }

    fun startParse(projectId: Long) {
        updateRuntimeState(projectId) {
            it.copy(
                realParsing = true,
                parseProgress = 0f
            )
        }
    }

    fun finishParse(projectId: Long) {
        val resolvedUserId = requireCurrentUserId() ?: return
        val state = runtimeStateOf(projectId)

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.parseProject(
                userId = resolvedUserId,
                projectId = projectId,
                parseMode = state.parseMode.name,
                sourceAssetIds = state.selectedParseSourceAssetIds,
                modelIds = state.selectedParseModelIds
            ).onSuccess { detail ->
                currentProjectDetail = detail

                currentMediaAssets.clear()
                currentMediaAssets.addAll(detail.mediaAssets)

                currentModelAssets.clear()
                currentModelAssets.addAll(detail.modelAssets)

                syncProjectAssetCounts(
                    projectId = detail.project.id,
                    mediaAssets = detail.mediaAssets,
                    modelAssets = detail.modelAssets
                )

                updateRuntimeState(projectId) {
                    it.copy(
                        realParsing = false,
                        parseProgress = 1f
                    )
                }

                updateProjectInList(projectId) { detail.project }
            }.onFailure { error ->
                updateRuntimeState(projectId) {
                    it.copy(
                        realParsing = false,
                        parseProgress = 0f
                    )
                }
                errorMessage = error.message ?: "解析失败"
            }

            isLoading = false
        }
    }

    fun clearError() {
        errorMessage = null
    }

    private fun requireCurrentUserId(): Long? {
        val userId = currentUserId
        if (userId == null) {
            errorMessage = "当前商家账号缺少用户 ID，请重新登录"
        }
        return userId
    }

    private fun resetUserScopedState() {
        projects.clear()
        currentMediaAssets.clear()
        currentModelAssets.clear()
        projectMediaCountMap.clear()
        projectModelCountMap.clear()
        localPreviewUriMap.clear()
        pendingLocalPreviewQueue.clear()
        runtimeStates.clear()
        selectedProjectId = null
        currentProjectDetail = null
        previewModel = null
        pageState = ContentPageState.PROJECT_LIST
        showEntrySheet = false
        showCreateProjectDialog = false
        showRenameProjectDialog = false
        showDeleteProjectDialog = false
        isUploading = false
        isRebuilding = false
        errorMessage = null
    }

    fun publishProject(
        projectId: Long,
        onSuccess: () -> Unit = {}
    ) {
        val resolvedUserId = requireCurrentUserId() ?: return
        viewModelScope.launch {
            runCatching {
                repository.publishProject(resolvedUserId, projectId)
            }.onSuccess { response ->
                if (response.success && response.data != null) {
                    val updatedProject = response.data

                    val updatedList = projects.map {
                        if (it.id == projectId) updatedProject.toMerchantProjectDto() else it
                    }
                    projects.clear()
                    projects.addAll(updatedList)

                    currentProjectDetail = currentProjectDetail?.copy(
                        project = updatedProject.toMerchantProjectDto()
                    )

                    onSuccess()
                } else {
                    errorMessage = response.message.ifBlank { "发布失败" }
                }
            }.onFailure { e ->
                errorMessage = e.message ?: "发布失败"
            }
        }
    }
}
