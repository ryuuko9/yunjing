package com.example.yunjing.ui.merchant.model

/**
 * 提供商家项目状态相关的统一判断，避免各页面散落硬编码字符串。
 */
private const val STATUS_PUBLISHED = "PUBLISHED"
private const val STATUS_COMPLETED = "COMPLETED"

/**
 * 判断项目是否已发布。
 */
fun isPublishedStatus(status: String?): Boolean {
    return status.equals(STATUS_PUBLISHED, ignoreCase = true)
}

/**
 * 判断流程状态是否已完成。
 */
fun isCompletedStatus(status: String?): Boolean {
    return status.equals(STATUS_COMPLETED, ignoreCase = true)
}

/**
 * 判断项目是否已经具备重建结果。
 */
fun hasRebuildResult(hasRebuildOutput: Int?, rebuildStatus: String?): Boolean {
    return hasRebuildOutput == 1 || isCompletedStatus(rebuildStatus)
}

/**
 * 判断项目是否已经生成可供发布的解析结果。
 */
fun hasParseResult(explodedImageUrl: String?, tutorialVideoUrl: String?): Boolean {
    return !explodedImageUrl.isNullOrBlank() || !tutorialVideoUrl.isNullOrBlank()
}
