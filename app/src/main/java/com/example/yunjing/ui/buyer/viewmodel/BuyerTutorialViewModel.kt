package com.example.yunjing.ui.buyer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yunjing.ui.buyer.model.BuyerTutorialDto
import com.example.yunjing.ui.buyer.repository.BuyerTutorialRepository
import com.example.yunjing.ui.merchant.model.ApiResponse
import com.example.yunjing.ui.merchant.model.MerchantProjectDto
import kotlinx.coroutines.launch

class BuyerTutorialViewModel(
    private val repository: BuyerTutorialRepository
) : ViewModel() {

    val tutorials = mutableStateListOf<BuyerTutorialDto>()

    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun clearError() {
        errorMessage = null
    }

    fun loadTutorials(buyerUserId: Long) {
        launchRequest(defaultErrorMessage = "加载教程失败") {
            val response = repository.listTutorials(buyerUserId)
            handleRequiredDataResponse(response, fallbackErrorMessage = "加载教程失败") { items ->
                tutorials.clear()
                tutorials.addAll(items)
            }
        }
    }

    fun importTutorial(
        publishCode: String,
        buyerUserId: Long,
        onSuccess: (() -> Unit)? = null
    ) {
        launchRequest(defaultErrorMessage = "导入教程失败") {
            val projectResponse = repository.getPublishedProject(publishCode)
            val projectDetail = projectResponse.data
            if (!projectResponse.success || projectDetail == null) {
                errorMessage = projectResponse.message.ifBlank { "当前项目已删除或已下线，二维码已失效" }
                return@launchRequest
            }

            if (!projectDetail.project.isImportableForBuyer()) {
                errorMessage = "当前项目已删除或已下线，二维码已失效"
                return@launchRequest
            }

            val response = repository.importTutorial(publishCode, buyerUserId)
            handleRequiredDataResponse(response, fallbackErrorMessage = "导入教程失败") { item ->
                val index = tutorials.indexOfFirst { it.id == item.id }
                if (index >= 0) {
                    tutorials[index] = item
                } else {
                    tutorials.add(0, item)
                }
                onSuccess?.invoke()
            }
        }
    }

    fun deleteTutorial(
        tutorialId: Long,
        buyerUserId: Long,
        onSuccess: (() -> Unit)? = null
    ) {
        launchRequest(defaultErrorMessage = "删除教程失败") {
            val response = repository.deleteTutorial(tutorialId, buyerUserId)
            handleResponse(response, fallbackErrorMessage = "删除教程失败") {
                tutorials.removeAll { it.id == tutorialId }
                onSuccess?.invoke()
            }
        }
    }

    private fun launchRequest(
        defaultErrorMessage: String,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                block()
            } catch (throwable: Throwable) {
                errorMessage = throwable.message ?: defaultErrorMessage
            } finally {
                isLoading = false
            }
        }
    }

    private fun handleResponse(
        response: ApiResponse<*>,
        fallbackErrorMessage: String,
        onSuccess: () -> Unit
    ) {
        if (response.success) {
            onSuccess()
            return
        }

        errorMessage = response.message.ifBlank { fallbackErrorMessage }
    }

    private fun <T> handleRequiredDataResponse(
        response: ApiResponse<T>,
        fallbackErrorMessage: String,
        onSuccess: (T) -> Unit
    ) {
        val data = response.data
        if (response.success && data != null) {
            onSuccess(data)
            return
        }

        errorMessage = response.message.ifBlank { fallbackErrorMessage }
    }

    private fun MerchantProjectDto.isImportableForBuyer(): Boolean {
        val normalizedPublishStatus = publishStatus.trim().uppercase()
        if (normalizedPublishStatus != "PUBLISHED") {
            return false
        }

        val normalizedStatus = status.trim().uppercase()
        return normalizedStatus !in setOf("DELETED", "REMOVED", "ARCHIVED", "INACTIVE")
    }
}
