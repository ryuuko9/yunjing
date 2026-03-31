package com.example.yunjing.ui.buyer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yunjing.ui.buyer.model.BuyerTutorialDto
import com.example.yunjing.ui.buyer.repository.BuyerTutorialRepository
import kotlinx.coroutines.launch

class BuyerTutorialViewModel(
    private val repository: BuyerTutorialRepository
) : ViewModel() {

    val tutorials = mutableStateListOf<BuyerTutorialDto>()

    var selectedTutorial by mutableStateOf<BuyerTutorialDto?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun clearError() {
        errorMessage = null
    }

    fun loadTutorials(buyerUserId: Long) {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                repository.listTutorials(buyerUserId)
            }.onSuccess { response ->
                if (response.success && response.data != null) {
                    tutorials.clear()
                    tutorials.addAll(response.data)
                } else {
                    errorMessage = response.message.ifBlank { "加载教程失败" }
                }
            }.onFailure {
                errorMessage = it.message ?: "加载教程失败"
            }
            isLoading = false
        }
    }

    fun importTutorial(
        publishCode: String,
        buyerUserId: Long,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                repository.importTutorial(publishCode, buyerUserId)
            }.onSuccess { response ->
                if (response.success && response.data != null) {
                    val item = response.data
                    val index = tutorials.indexOfFirst { it.id == item.id }
                    if (index >= 0) {
                        tutorials[index] = item
                    } else {
                        tutorials.add(0, item)
                    }
                    onSuccess?.invoke()
                } else {
                    errorMessage = response.message.ifBlank { "导入教程失败" }
                }
            }.onFailure {
                errorMessage = it.message ?: "导入教程失败"
            }
            isLoading = false
        }
    }

    fun loadTutorialDetail(tutorialId: Long, buyerUserId: Long) {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                repository.getTutorialDetail(tutorialId, buyerUserId)
            }.onSuccess { response ->
                if (response.success && response.data != null) {
                    selectedTutorial = response.data
                } else {
                    errorMessage = response.message.ifBlank { "加载教程详情失败" }
                }
            }.onFailure {
                errorMessage = it.message ?: "加载教程详情失败"
            }
            isLoading = false
        }
    }
}