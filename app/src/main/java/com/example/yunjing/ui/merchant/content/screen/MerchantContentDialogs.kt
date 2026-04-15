package com.example.yunjing.ui.merchant.content.screen

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.yunjing.ui.merchant.model.ProjectMediaAssetDto

/**
 * 本文件负责内容库中删除确认、素材预览、爆炸图预览等弹窗逻辑。
 */

/**
 * 这个函数负责确认删除项目，避免误删项目数据。
 */
@Composable
fun DeleteProjectDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除项目") },
        text = { Text("确认删除项目“$projectName”吗？删除后不可恢复。") },
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

/**
 * 这个函数负责预览已上传的图片或视频素材，并兼容本地 Uri 与后端地址。
 */
@Composable
fun BackendMediaPreviewDialog(
    item: ProjectMediaAssetDto,
    localUri: Uri?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val remoteUrl = remember(item.fileUrl) { normalizePreviewUrl(item.fileUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = {
            Text(item.fileName)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    item.assetType.equals("VIDEO", true) -> {
                        when {
                            localUri != null -> {
                                AndroidView(
                                    factory = { ctx ->
                                        android.widget.VideoView(ctx).apply {
                                            setVideoURI(localUri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                start()
                                            }
                                        }
                                    },
                                    update = { view ->
                                        view.setVideoURI(localUri)
                                        view.setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            view.start()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                )
                            }

                            !remoteUrl.isNullOrBlank() -> {
                                AndroidView(
                                    factory = { ctx ->
                                        android.widget.VideoView(ctx).apply {
                                            setVideoPath(remoteUrl)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                start()
                                            }
                                        }
                                    },
                                    update = { view ->
                                        view.setVideoPath(remoteUrl)
                                        view.setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            view.start()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                )
                            }

                            else -> {
                                Text(
                                    "当前视频暂无可用预览地址",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    localUri != null -> {
                        AsyncImage(
                            model = localUri,
                            contentDescription = item.fileName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    !remoteUrl.isNullOrBlank() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(remoteUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.fileName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    else -> {
                        Text(
                            "当前图片暂无可用预览地址",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

/**
 * 这个函数负责把后端返回的图片地址规范化为模拟器可访问的预览地址。
 */
fun normalizePreviewUrl(rawUrl: String?): String? {
    if (rawUrl.isNullOrBlank()) return null

    return when {
        rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> {
            rawUrl.replace("localhost", "10.0.2.2")
//            rawUrl.replace("localhost", "172.20.10.3")
        }

        rawUrl.startsWith("/") -> {
            "http://10.0.2.2:8080$rawUrl"
//            "http://172.20.10.3:8080$rawUrl"
        }

        else -> rawUrl
    }
}

/**
 * 这个函数负责全屏展示爆炸图，并支持缩放与拖拽预览。
 */
@Composable
fun ExplodedImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)

        if (newScale == 1f) {
            offsetX = 0f
            offsetY = 0f
        } else {
            offsetX += panChange.x
            offsetY += panChange.y
        }

        scale = newScale
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
                .navigationBarsPadding()
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "爆炸图全屏预览",
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(transformState)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.Fit
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("关闭", color = Color.White)
                }
            }
        }
    }
}

/**
 * 这个函数负责把后端返回的 Base64 图片解码为 Compose 可显示的位图对象。
 */
fun decodeBase64ToImageBitmap(base64Value: String?): ImageBitmap? {
    return try {
        if (base64Value.isNullOrBlank()) return null

        val pureBase64 = base64Value.substringAfter("base64,", base64Value)
        val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
