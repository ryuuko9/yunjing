package com.example.yunjing.ui.merchant.component

import android.net.Uri
import android.widget.ImageView
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.yunjing.ui.merchant.model.MerchantContentProject
import com.example.yunjing.ui.merchant.model.ParseMode
import com.example.yunjing.ui.merchant.model.PendingMediaType
import com.example.yunjing.ui.merchant.model.PendingUploadItem
import com.example.yunjing.ui.merchant.model.ProjectModelItem
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
fun ProjectListItem(
    project: MerchantContentProject,
    onClick: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onClick)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(project.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${project.summary} · ${project.status}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusBadge(text = project.status)
            Spacer(Modifier.width(8.dp))
            Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
fun SelectableUploadRow(
    item: PendingUploadItem,
    selected: Boolean,
    onToggle: () -> Unit,
    onPreview: () -> Unit
) {
    val typeText = if (item.type == PendingMediaType.IMAGE) "图片" else "视频"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .pressClick(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggle() }
        )

        Spacer(Modifier.width(8.dp))

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
    }
}

@Composable
fun ModelFileRow(
    item: ProjectModelItem,
    selected: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .pressClick(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggle() }
        )

        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.ViewInAr, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                ".glb 模型文件",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
fun WorkbenchSectionCard(
    title: String,
    desc: String,
    actionArea: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        desc,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = actionArea
                )
            }

            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun ProgressBlock(
    title: String,
    progress: Float,
    hint: String?
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
    onUploadImageClick: () -> Unit,
    onUploadVideoClick: () -> Unit,
    onCaptureImageClick: () -> Unit,
    onCaptureVideoClick: () -> Unit,
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
                icon = Icons.Filled.Image,
                title = "上传图片",
                subtitle = "从本地相册选择图片素材",
                onClick = onUploadImageClick
            )

            SheetActionItem(
                icon = Icons.Filled.Videocam,
                title = "上传视频",
                subtitle = "从本地相册选择视频素材",
                onClick = onUploadVideoClick
            )

            SheetActionItem(
                icon = Icons.Filled.PhotoCamera,
                title = "拍照上传",
                subtitle = "使用相机拍摄图片素材",
                onClick = onCaptureImageClick
            )

            SheetActionItem(
                icon = Icons.Filled.PlayCircleOutline,
                title = "拍摄视频上传",
                subtitle = "使用相机录制视频素材",
                onClick = onCaptureVideoClick
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

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建项目") },
        text = {
            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("项目名称") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalName = projectName.trim().ifEmpty { "未命名项目" }
                    onConfirm(finalName)
                }
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun RenameProjectDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var projectName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("项目设置") },
        text = {
            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("项目名称") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalName = projectName.trim().ifEmpty { currentName }
                    onConfirm(finalName)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun MaterialFolderCard(
    folderName: String,
    mediaCount: Int,
    onClick: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onClick)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(folderName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "共 $mediaCount 个素材，可统一预览",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProjectSettingMenu(
    modifier: Modifier = Modifier,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        MiniChip(
            text = "项目设置",
            icon = Icons.Filled.Description,
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("重命名项目") },
                onClick = {
                    expanded = false
                    onRename()
                }
            )
            DropdownMenuItem(
                text = { Text("删除项目") },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
fun DeleteMediaDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除素材") },
        text = { Text("确认删除素材“$fileName”吗？删除后不可恢复。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}