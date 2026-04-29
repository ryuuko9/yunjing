package com.example.yunjing.ui.buyer

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.yunjing.network.AppServerConfig
import com.example.yunjing.ui.buyer.model.BuyerTutorialDto
import com.example.yunjing.ui.pressClick
import com.example.yunjing.ui.unity.canOpenUnityPlayer
import com.example.yunjing.ui.unity.createUnityPlayerIntent
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.math.max

/**
 * 买家教程模块公共支持文件，负责教程数据归一化、扫码解析和资源预览辅助逻辑。
 */

/**
 * 文件作用：
 * 提供教程模块的共享数据结构与辅助逻辑，
 * 包括 DTO 转换、二维码解析、播放器跳转、爆炸图预览等与页面渲染解耦的能力。
 */

/**
 * 作用：
 * 将教程展示层需要的数据聚合成 UI 专用模型，避免页面直接依赖 DTO 原始结构。
 */
internal data class BuyerTutorialUi(
    val id: Long,
    val publishCode: String,
    val tutorialName: String,
    val projectDesc: String?,
    val explodedImageUrl: String?,
    val tutorialVideoUrl: String?,
    val tutorialTitle: String?,
    val publishUrl: String?,
    val addedAtText: String
)

/**
 * 作用：
 * 统一修正教程资源预览地址，兼容本地服务地址和相对路径。
 */
private fun normalizeBuyerPreviewUrl(rawUrl: String?): String? {
    return AppServerConfig.normalizeBackendUrl(rawUrl)
}

/**
 * 把空白文本折叠为 null，避免页面层到处 trim 和判空。
 */
private fun String?.normalizeBuyerText(): String? {
    return this?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * 优先使用后端返回的买家访问地址，缺失时回退到基于发布码拼接的标准地址。
 */
private fun resolveBuyerPublishUrl(publishCode: String, publishUrl: String?): String? {
    val normalizedPublishUrl = AppServerConfig.normalizeBackendUrl(publishUrl)
    if (normalizedPublishUrl != null) {
        return normalizedPublishUrl
    }

    val normalizedPublishCode = publishCode.trim()
    if (normalizedPublishCode.isEmpty()) {
        return null
    }

    return AppServerConfig.normalizeBackendUrl("/api/buyer/projects/$normalizedPublishCode")
}

/**
 * 作用：
 * 提供爆炸图的双击缩放和拖拽预览能力。
 */
@Composable
internal fun BuyerZoomableImageViewer(
    imageUrl: String,
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 4f
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            reset()
                        } else {
                            scale = 2f
                            offset = Offset.Zero
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                    if (newScale <= 1f) {
                        scale = 1f
                        offset = Offset.Zero
                        return@detectTransformGestures
                    }

                    scale = newScale
                    offset += pan

                    val maxX = max(0f, containerSize.width * (scale - 1f) / 2f)
                    val maxY = max(0f, containerSize.height * (scale - 1f) / 2f)

                    offset = Offset(
                        x = offset.x.coerceIn(-maxX, maxX),
                        y = offset.y.coerceIn(-maxY, maxY)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "爆炸图全屏预览",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * 作用：
 * 以全屏弹窗方式展示爆炸图预览。
 */
@Composable
internal fun BuyerExplodedImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
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
                .statusBarsPadding()
        ) {
            BuyerZoomableImageViewer(
                imageUrl = imageUrl,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "关闭",
                    color = Color.White,
                    modifier = Modifier.pressClick(onClick = onDismiss)
                )
            }
        }
    }
}

/**
 * 作用：
 * 从扫码结果中提取 publishCode，兼容纯码值、路径参数和 query 参数格式。
 */
internal fun extractPublishCodeFromScan(raw: String?): String? {
    val value = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    // 1) 如果本身就是 publishCode
    val simpleCodeRegex = Regex("^[A-Za-z0-9]{16,64}$")
    if (simpleCodeRegex.matches(value)) {
        return value
    }

    // 2) 如果是 URL，取最后一段 path
    runCatching {
        val uri = Uri.parse(value)
        val lastSegment = uri.lastPathSegment
        if (!lastSegment.isNullOrBlank() && simpleCodeRegex.matches(lastSegment)) {
            return lastSegment
        }

        // 3) 如果是 query 参数 code
        val code = uri.getQueryParameter("code")
        if (!code.isNullOrBlank() && simpleCodeRegex.matches(code)) {
            return code
        }
    }

    return null
}

/**
 * 作用：
 * 将接口返回的 BuyerTutorialDto 转换为页面直接使用的 BuyerTutorialUi。
 */
/**
 * 把后端教程数据转换成页面稳定消费的 UI 模型，并补齐快照缺失时的兜底字段。
 */
internal fun BuyerTutorialDto.toBuyerTutorialUi(): BuyerTutorialUi {
    val normalizedTutorialName = tutorialName.trim()
    val normalizedTutorialTitle = tutorialTitle.normalizeBuyerText() ?: normalizedTutorialName
    return BuyerTutorialUi(
        id = id,
        publishCode = publishCode,
        tutorialName = normalizedTutorialName,
        projectDesc = projectDesc.normalizeBuyerText(),
        explodedImageUrl = normalizeBuyerPreviewUrl(explodedImageUrl),
        tutorialVideoUrl = normalizeBuyerPreviewUrl(tutorialVideoUrl),
        tutorialTitle = normalizedTutorialTitle,
        publishUrl = resolveBuyerPublishUrl(publishCode, publishUrl),
        addedAtText = addedAt ?: "刚刚导入"
    )
}

/**
 * 作用：
 * 使用 ML Kit 从本地图片中识别二维码内容。
 */
internal fun decodeQrFromImageUriWithMlKit(
    context: Context,
    uri: Uri,
    onResult: (String?) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)

        val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstOrNull()?.rawValue
                onResult(rawValue)
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult(null)
            }
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(null)
    }
}

/**
 * 作用：
 * 统一显示短时 Toast，避免页面内重复拼接 Toast 代码。
 */
internal fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * 作用：
 * 打开教程播放器页面，并在打开失败时给出统一错误提示。
 */
internal fun Context.openBuyerTutorialPlayer(tutorial: BuyerTutorialUi) {
    try {
        val intent = createUnityPlayerIntent(
            publishCode = tutorial.publishCode,
            tutorialVideoUrl = tutorial.tutorialVideoUrl,
            tutorialTitle = tutorial.tutorialTitle
        )
        if (!canOpenUnityPlayer(intent)) {
            showShortToast("鏈壘鍒?Unity 椤甸潰")
            return
        }
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        showShortToast("未找到 Unity 页面")
    } catch (e: Exception) {
        showShortToast("打开 Unity 失败：${e.message ?: "未知错误"}")
    }
}
