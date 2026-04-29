package com.example.yunjing.ui.merchant.model

import com.example.yunjing.network.AppServerConfig

/**
 * 负责承接商家项目接口返回，并在进入页面层前统一做空值、状态和值对象归一化。
 */
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

/**
 * 把后端响应转换为前端页面稳定消费的项目对象，避免界面层直接处理空白字符串和相对地址。
 */
fun ProjectResponseDto.toMerchantProjectDto(): MerchantProjectDto {
    val normalizedPublishCode = publishCode.normalizeOptionalText()
    return MerchantProjectDto(
        id = id,
        userId = userId,
        projectName = projectName,
        projectDesc = projectDesc.normalizeOptionalText(),
        coverUrl = AppServerConfig.normalizeBackendUrl(coverUrl),
        status = status,
        summary = summary.normalizeOptionalText(),
        hasRebuildOutput = hasRebuildOutput,
        rebuildStatus = rebuildStatus.normalizeStatus(),
        parseStatus = parseStatus.normalizeStatus(),
        publishStatus = publishStatus.normalizeStatus(),
        parseMode = parseMode.normalizeOptionalText(),
        parseResultText = parseResultText.normalizeOptionalText(),
        explodedImageUrl = AppServerConfig.normalizeBackendUrl(explodedImageUrl),
        tutorialVideoUrl = AppServerConfig.normalizeBackendUrl(tutorialVideoUrl),
        tutorialTitle = tutorialTitle.normalizeOptionalText(),
        createdAt = createdAt.normalizeOptionalText(),
        updatedAt = updatedAt.normalizeOptionalText(),
        publishCode = normalizedPublishCode,
        publishUrl = resolvePublishUrl(normalizedPublishCode, publishUrl),
        qrCodeBase64 = qrCodeBase64.normalizeOptionalText(),
        publishedAt = publishedAt.normalizeOptionalText()
    )
}

/**
 * 统一收敛后端返回的状态字段，避免 null 直接变成 "null" 文本。
 */
private fun String?.normalizeStatus(): String {
    return this?.trim().orEmpty()
}

/**
 * 统一把空白文本折叠为 null，减少页面层重复判空。
 */
private fun String?.normalizeOptionalText(): String? {
    return this?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * 优先使用后端返回的发布地址，没有时回退到基于发布码拼装的买家访问地址。
 */
private fun resolvePublishUrl(publishCode: String?, publishUrl: String?): String? {
    val normalizedPublishUrl = AppServerConfig.normalizeBackendUrl(publishUrl)
    if (normalizedPublishUrl != null) {
        return normalizedPublishUrl
    }
    val safePublishCode = publishCode ?: return null
    return AppServerConfig.normalizeBackendUrl("/api/buyer/projects/$safePublishCode")
}
