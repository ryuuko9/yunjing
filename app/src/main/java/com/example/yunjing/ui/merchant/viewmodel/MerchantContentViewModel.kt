package com.example.yunjing.ui.merchant.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yunjing.ui.merchant.model.MerchantProjectDetailDto
import com.example.yunjing.ui.merchant.model.MerchantProjectDto
import com.example.yunjing.ui.merchant.model.ProjectMediaAssetDto
import com.example.yunjing.ui.merchant.model.ProjectModelAssetDto
import com.example.yunjing.ui.merchant.repository.MerchantContentRepository
import kotlinx.coroutines.launch

class MerchantContentViewModel(
    private val repository: MerchantContentRepository
) : ViewModel() {

    companion object {
        private const val DEMO_USER_ID = 1L
    }

    val projects = mutableStateListOf<MerchantProjectDto>()
    val currentMediaAssets = mutableStateListOf<ProjectMediaAssetDto>()
    val currentModelAssets = mutableStateListOf<ProjectModelAssetDto>()

    var selectedProjectId by mutableStateOf<Long?>(null)
        private set

    var currentProjectDetail by mutableStateOf<MerchantProjectDetailDto?>(null)
        private set

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

                    val currentSelectedId = selectedProjectId
                    if (currentSelectedId == null && list.isNotEmpty()) {
                        selectProject(list.first().id)
                    } else if (currentSelectedId != null && list.none { it.id == currentSelectedId }) {
                        selectedProjectId = null
                        currentProjectDetail = null
                        currentMediaAssets.clear()
                        currentModelAssets.clear()
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
        if (name.isBlank()) {
            errorMessage = "项目名称不能为空"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.createProject(
                userId = userId,
                projectName = name.trim(),
                projectDesc = desc?.trim()?.takeIf { it.isNotBlank() }
            ).onSuccess { project ->
                loadProjects(userId)
                selectedProjectId = project.id
                loadProjectDetail(project.id)
                onSuccess?.invoke()
            }.onFailure { error ->
                errorMessage = error.message ?: "创建项目失败"
            }

            isLoading = false
        }
    }

    fun selectProject(projectId: Long) {
        selectedProjectId = projectId
        loadProjectDetail(projectId)
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

            repository.uploadMedia(
                context = context,
                projectId = projectId,
                assetType = assetType,
                uri = uri
            ).onSuccess {
                loadProjectDetail(projectId)
                onSuccess?.invoke()
            }.onFailure { error ->
                errorMessage = error.message ?: "素材上传失败"
            }

            isUploading = false
        }
    }

    fun rebuildProject(
        projectId: Long,
        sourceAssetIds: List<Long>? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            isRebuilding = true
            errorMessage = null

            repository.rebuildProject(projectId, sourceAssetIds)
                .onSuccess { models ->
                    currentModelAssets.clear()
                    currentModelAssets.addAll(models)
                    loadProjectDetail(projectId)
                    onSuccess?.invoke()
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "重建失败"
                }

            isRebuilding = false
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

    fun clearError() {
        errorMessage = null
    }
}