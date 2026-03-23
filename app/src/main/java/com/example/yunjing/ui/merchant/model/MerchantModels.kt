package com.example.yunjing.ui.merchant.model

import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector

// 所有数据类、枚举

data class MerchantTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

data class MerchantQueueItem(
    val title: String,
    val subtitle: String
)

data class ContentItem(
    val title: String,
    val subtitle: String,
    val badge: String
)

enum class PendingMediaType {
    IMAGE, VIDEO
}

data class PendingUploadItem(
    val uri: Uri,
    val type: PendingMediaType,
    val name: String,
    val source: String
)