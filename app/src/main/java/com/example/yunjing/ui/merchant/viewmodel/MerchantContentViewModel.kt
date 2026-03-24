package com.example.yunjing.ui.merchant.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.yunjing.ui.merchant.model.ContentItem
import com.example.yunjing.ui.merchant.model.ContentPageState
import com.example.yunjing.ui.merchant.model.MerchantContentProject
import com.example.yunjing.ui.merchant.model.PendingUploadItem

class MerchantContentViewModel : ViewModel() {

    val projects = mutableStateListOf(
        MerchantContentProject(
            id = "project_1",
            name = "螺旋风扇安装教程",
            status = "已发布",
            summary = "版本 v1.2"
        )
    )

    var selectedProjectId by mutableStateOf<String?>(null)
    var pageState by mutableStateOf(ContentPageState.PROJECT_LIST)

    var showCreateProjectDialog by mutableStateOf(false)
    var showRenameProjectDialog by mutableStateOf(false)
    var showDeleteProjectDialog by mutableStateOf(false)
    var showEntrySheet by mutableStateOf(false)

    var previewItem by mutableStateOf<PendingUploadItem?>(null)
    var currentCaptureImageUri by mutableStateOf<Uri?>(null)
    var pendingCameraAction by mutableStateOf<(() -> Unit)?>(null)

    val recentItems = listOf(
        ContentItem("桌面支架拆装说明", "版本 v0.9 · 待优化", "待解析"),
        ContentItem("蓝牙音箱教学内容", "版本 v1.0 · 审核中", "待审核")
    )

    fun currentProject(): MerchantContentProject? {
        return projects.find { it.id == selectedProjectId }
    }

    fun updateProject(
        projectId: String,
        transform: (MerchantContentProject) -> MerchantContentProject
    ) {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index >= 0) {
            projects[index] = transform(projects[index])
        }
    }

    fun createProject(projectName: String): String {
        val nextIndex = projects.size + 1
        val finalName = projectName.trim().ifEmpty { "未命名项目$nextIndex" }

        val newProject = MerchantContentProject(
            id = "project_$nextIndex",
            name = finalName,
            status = "编辑中",
            summary = "待上传内容"
        )
        projects.add(0, newProject)
        selectedProjectId = newProject.id
        pageState = ContentPageState.PROJECT_DETAIL
        return finalName
    }

    fun renameCurrentProject(newName: String): Boolean {
        val project = currentProject() ?: return false
        updateProject(project.id) {
            it.copy(name = newName.trim().ifEmpty { it.name })
        }
        return true
    }

    fun deleteCurrentProject(): Boolean {
        val project = currentProject() ?: return false
        projects.removeAll { it.id == project.id }
        selectedProjectId = null
        pageState = ContentPageState.PROJECT_LIST
        return true
    }
}