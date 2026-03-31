package com.example.yunjing.ui.merchant.model

data class ProjectResponseDto(
    val id: Long,
    val userId: Long,
    val projectName: String,
    val projectDesc: String?,
    val coverUrl: String?,
    val status: String,
    val summary: String?,
    val hasRebuildOutput: Int?,
    val rebuildStatus: String?,
    val parseStatus: String?,
    val publishStatus: String?,
    val parseMode: String?,
    val parseResultText: String?,
    val explodedImageUrl: String?,
    val tutorialVideoUrl: String?,
    val tutorialTitle: String?,
    val publishCode: String?,
    val publishUrl: String?,
    val qrCodeBase64: String?,
    val publishedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

fun ProjectResponseDto.toMerchantProjectDto(): MerchantProjectDto {
    return MerchantProjectDto(
        id = id,
        userId = userId,
        projectName = projectName,
        projectDesc = projectDesc,
        coverUrl = coverUrl,
        status = status,
        summary = summary,
        hasRebuildOutput = hasRebuildOutput,
        rebuildStatus = rebuildStatus.toString(),
        parseStatus = parseStatus.toString(),
        publishStatus = publishStatus.toString(),
        parseMode = parseMode,
        parseResultText = parseResultText,
        explodedImageUrl = explodedImageUrl,
        tutorialVideoUrl = tutorialVideoUrl,
        tutorialTitle = tutorialTitle,
        createdAt = createdAt,
        updatedAt = updatedAt,
        publishCode = publishCode,
        publishUrl = publishUrl,
        qrCodeBase64 = qrCodeBase64,
        publishedAt = publishedAt
    )
}