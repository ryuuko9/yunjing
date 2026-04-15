package com.example.yunjing.ui.merchant.content.model

/**
 * 本文件用于定义内容库相关接口请求体。
 */
data class RenameProjectRequest(
    val projectName: String
)

data class ParseProjectRequest(
    val parseMode: String,
    val sourceAssetIds: List<Long>,
    val modelIds: List<Long>
)
