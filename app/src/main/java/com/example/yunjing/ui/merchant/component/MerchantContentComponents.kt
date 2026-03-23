package com.example.yunjing.ui.merchant.component

import android.net.Uri
import android.widget.ImageView
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.yunjing.ui.merchant.model.MerchantAssetItem
import com.example.yunjing.ui.merchant.model.MerchantAssetType
import com.example.yunjing.ui.merchant.model.MerchantContentProject
import com.example.yunjing.ui.merchant.model.ParseMode
import com.example.yunjing.ui.merchant.model.PendingMediaType
import com.example.yunjing.ui.merchant.model.PendingUploadItem
import com.example.yunjing.ui.pressClick

@Composable
fun ContentRow(
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .pressClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        StatusBadge(text = badge)
        Spacer(Modifier.width(8.dp))
        Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PendingUploadRow(
    item: PendingUploadItem,
    onPreview: () -> Unit,
    onRemove: () -> Unit
) {
    val icon = if (item.type == PendingMediaType.IMAGE) Icons.Filled.Image else Icons.Filled.Videocam
    val typeText = if (item.type == PendingMediaType.IMAGE) "图片" else "视频"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                "$typeText · ${item.source}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onPreview) {
            Text("预览")
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFF3B30).copy(alpha = 0.12f))
                .pressClick(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = "删除", tint = Color(0xFFFF3B30))
        }
    }
}

@Composable
fun ProjectHeaderCard(
    project: MerchantContentProject?,
    onNewProject: () -> Unit,
    onPublish: () -> Unit,
    canPublish: Boolean
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("项目工作区", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = project?.let { "当前项目：${it.name} · ${it.summary}" }
                    ?: "当前未创建项目，请先新建后再进行上传、重建或解析。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniChip(
                    text = "新建项目",
                    icon = Icons.Filled.Inventory2,
                    onClick = onNewProject,
                    modifier = Modifier.weight(1f)
                )
                MiniChip(
                    text = "发布",
                    icon = Icons.Filled.AutoAwesome,
                    onClick = onPublish,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!canPublish) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "发布前置条件：当前项目需先完成上传、重建或解析中的至少一项成果。",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WorkbenchSectionCard(
    title: String,
    desc: String,
    actionText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        desc,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                MiniChip(
                    text = actionText,
                    icon = icon,
                    onClick = onAction
                )
            }

            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun AssetRow(
    item: MerchantAssetItem,
    onPreview: () -> Unit,
    onRemove: () -> Unit
) {
    val icon = when (item.type) {
        MerchantAssetType.IMAGE -> Icons.Filled.Image
        MerchantAssetType.VIDEO -> Icons.Filled.Videocam
        MerchantAssetType.MODEL -> Icons.Filled.ViewInAr
    }

    val typeText = when (item.type) {
        MerchantAssetType.IMAGE -> "图片"
        MerchantAssetType.VIDEO -> "视频"
        MerchantAssetType.MODEL -> "模型"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                "$typeText · ${item.source}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onPreview) {
            Text("预览")
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFF3B30).copy(alpha = 0.12f))
                .pressClick(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = "删除", tint = Color(0xFFFF3B30))
        }
    }
}

@Composable
fun ProgressBlock(
    title: String,
    progress: Float,
    hint: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${(progress * 100).toInt()}% · $hint",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RebuildResultCard(
    modelName: String,
    statusText: String,
    onBrowseModel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ViewInAr, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(modelName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text(statusText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            MiniChip(
                text = "浏览模型",
                icon = Icons.Filled.ViewInAr,
                onClick = onBrowseModel
            )
        }
    }
}

@Composable
fun ParseResultCard(
    mode: ParseMode,
    statusText: String,
    onOpenVideo: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (mode == ParseMode.EXPLODED_GUIDE) "解析结果：爆炸图说明书" else "解析结果：可视化教程",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(statusText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))

            if (mode == ParseMode.EXPLODED_GUIDE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("爆炸图预览占位", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                MiniChip(
                    text = "打开教程播放器",
                    icon = Icons.Filled.PlayCircleOutline,
                    onClick = onOpenVideo
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadEntrySheet(
    onDismiss: () -> Unit,
    onUploadClick: () -> Unit,
    onCaptureClick: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("选择操作", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            SheetActionItem(
                icon = Icons.Filled.UploadFile,
                title = "上传",
                subtitle = "从本地选择照片或视频",
                onClick = onUploadClick
            )

            SheetActionItem(
                icon = Icons.Filled.PhotoCamera,
                title = "拍摄",
                subtitle = "使用相机拍摄照片或视频",
                onClick = onCaptureClick
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadTypeSheet(
    title: String,
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            SheetActionItem(
                icon = Icons.Filled.Image,
                title = "照片",
                subtitle = "选择图片文件",
                onClick = onImageClick
            )

            SheetActionItem(
                icon = Icons.Filled.Videocam,
                title = "视频",
                subtitle = "选择视频文件",
                onClick = onVideoClick
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinueAddSheet(
    type: PendingMediaType,
    onDismiss: () -> Unit,
    onPickClick: () -> Unit,
    onCaptureClick: () -> Unit
) {
    val title = if (type == PendingMediaType.IMAGE) "继续添加照片" else "继续添加视频"
    val pickText = if (type == PendingMediaType.IMAGE) "从本地选择照片" else "从本地选择视频"
    val captureText = if (type == PendingMediaType.IMAGE) "继续拍摄照片" else "继续拍摄视频"
    val icon2 = if (type == PendingMediaType.IMAGE) Icons.Filled.PhotoCamera else Icons.Filled.Videocam

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            SheetActionItem(
                icon = Icons.Filled.UploadFile,
                title = "上传",
                subtitle = pickText,
                onClick = onPickClick
            )

            SheetActionItem(
                icon = icon2,
                title = "拍摄",
                subtitle = captureText,
                onClick = onCaptureClick
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPreviewSheet(
    item: PendingUploadItem,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "${if (item.type == PendingMediaType.IMAGE) "图片" else "视频"} · ${item.source}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.type == PendingMediaType.IMAGE) {
                    AndroidView(
                        factory = { context ->
                            ImageView(context).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                setImageURI(item.uri)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                setVideoURI(item.uri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            PrimaryPillButton(
                text = "关闭预览",
                onClick = onDismiss
            )
            Spacer(Modifier.height(14.dp))
        }
    }
}