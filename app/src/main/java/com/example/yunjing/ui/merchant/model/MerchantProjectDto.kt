package com.example.yunjing.ui.merchant.model

data class MerchantProjectDto(
    val id: Long,
    val userId: Long,
    val projectName: String,
    val projectDesc: String? = null,
    val coverUrl: String? = null,
    val status: String,
    val summary: String? = null,
    val hasRebuildOutput: Int,
    val rebuildStatus: String,
    val parseMode: String? = null,
    val parseResultText: String? = null,
    val explodedImageUrl: String? = null,
    val tutorialVideoUrl: String? = null,
    val tutorialTitle: String? = null,
    val parseStatus: String,
    val publishStatus: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)