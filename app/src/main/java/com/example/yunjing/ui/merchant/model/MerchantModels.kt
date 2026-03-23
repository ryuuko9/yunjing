package com.example.yunjing.ui.merchant.model

import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector

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

enum class MerchantAssetType {
    IMAGE, VIDEO, MODEL
}

data class MerchantAssetItem(
    val uri: Uri,
    val type: MerchantAssetType,
    val name: String,
    val source: String
)

enum class ParseMode {
    EXPLODED_GUIDE,
    VIDEO_GUIDE
}

data class MerchantContentProject(
    val id: String,
    val name: String,
    val status: String,
    val summary: String
)

data class RebuildResult(
    val modelName: String,
    val modelAssetName: String,
    val coverText: String,
    val statusText: String
)

data class ParseResult(
    val mode: ParseMode,
    val explodedImageName: String? = null,
    val tutorialVideoName: String? = null,
    val statusText: String
)