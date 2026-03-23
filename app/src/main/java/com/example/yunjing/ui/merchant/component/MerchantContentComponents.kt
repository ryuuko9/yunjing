package com.example.yunjing.ui.merchant.component

import android.net.Uri
import android.widget.ImageView
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.UploadFile
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
                text = if (item.type == PendingMediaType.IMAGE) "图片预览" else "视频预览",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            if (item.type == PendingMediaType.IMAGE) {
                ImagePreviewContent(uri = item.uri)
            } else {
                VideoPreviewContent(uri = item.uri)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ImagePreviewContent(uri: Uri) {
    AndroidView(
        factory = {
            ImageView(it).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                setImageURI(uri)
            }
        },
        update = { it.setImageURI(uri) },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    )
}

@Composable
private fun VideoPreviewContent(uri: Uri) {
    AndroidView(
        factory = {
            VideoView(it).apply {
                setVideoURI(uri)
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    start()
                }
            }
        },
        update = { videoView ->
            videoView.setVideoURI(uri)
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                videoView.start()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    )
}