package com.example.yunjing.ui.merchant.model

data class ProjectModelAssetDto(
    val id: Long,
    val projectId: Long,
    val modelName: String,
    val fileUrl: String,
    val previewUrl: String? = null,
    val sourceType: String,
    val visibleAfterRebuild: Int,
    val status: String? = null
)