package com.example.yunjing.ui.merchant.common.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 本文件用于定义 merchant 底部导航使用的标签模型。
 */
data class MerchantTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)
