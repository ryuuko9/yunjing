package com.example.yunjing.ui.merchant.model

data class ProjectMediaAssetDto(
    val id: Long,
    val projectId: Long,
    val assetType: String,
    val fileName: String,
    val fileUrl: String,
    val mimeType: String? = null,
    val fileSize: Long? = null,
    val sortOrder: Int? = null,
    val status: String? = null
)