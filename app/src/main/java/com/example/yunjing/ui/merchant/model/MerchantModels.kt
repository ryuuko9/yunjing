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

enum class ParseMode {
    EXPLODED_GUIDE,
    VIDEO_GUIDE
}

data class ProjectModelItem(
    val uri: Uri,
    val name: String
)

data class RebuildResult(
    val modelName: String,
    val statusText: String
)

data class ParseResult(
    val mode: ParseMode,
    val statusText: String
)

data class MerchantContentProject(
    val id: String,
    val name: String,
    val status: String,
    val summary: String,

    val uploads: List<PendingUploadItem> = emptyList(),

    val selectedRebuildUris: List<String> = emptyList(),

    val parseMode: ParseMode = ParseMode.EXPLODED_GUIDE,
    val selectedParseSourceUris: List<String> = emptyList(),
    val parseModels: List<ProjectModelItem> = emptyList(),
    val selectedParseModelUris: List<String> = emptyList(),

    val isRebuilding: Boolean = false,
    val rebuildProgress: Float = 0f,
    val rebuildResult: RebuildResult? = null,

    val isParsing: Boolean = false,
    val parseProgress: Float = 0f,

    val explodedGuideResult: ParseResult? = null,
    val videoGuideResult: ParseResult? = null,
)

enum class ContentPageState {
    PROJECT_LIST,
    PROJECT_DETAIL,
    MEDIA_FOLDER_MANAGE,
    MEDIA_FOLDER_SELECT_REBUILD,
    MEDIA_FOLDER_SELECT_PARSE
}

enum class ProjectMenuAction {
    RENAME,
    DELETE
}