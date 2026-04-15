package com.example.yunjing.ui.merchant.content.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.yunjing.ui.merchant.common.component.MiniChip
import com.example.yunjing.ui.merchant.model.ProjectModelAssetDto
import com.google.android.filament.LightManager
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader

/**
 * 本文件负责展示模型预览页，以及模型地址的规范化处理。
 */

/**
 * 这个函数负责加载并展示单个模型文件的 3D 预览画面。
 */
@Composable
fun ModelPreviewContent(
    model: ProjectModelAssetDto,
    onBack: () -> Unit
) {
    val modelUrl = remember(model.fileUrl) { normalizeModelUrl(model.fileUrl) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    val environment = rememberEnvironment(environmentLoader) {
        environmentLoader.createHDREnvironment("environments/qwantani_dusk_2_puresky_2k.hdr")!!
    }

    val cameraManipulator = rememberCameraManipulator()

    var modelInstance by remember(modelUrl) { mutableStateOf<ModelInstance?>(null) }
    var isLoading by remember(modelUrl) { mutableStateOf(false) }
    var loadError by remember(modelUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(modelUrl) {
        modelInstance = null
        loadError = null

        if (modelUrl.isNullOrBlank()) {
            loadError = "模型地址为空"
            return@LaunchedEffect
        }

        isLoading = true
        try {
            modelLoader.loadModelInstanceAsync(
                fileLocation = modelUrl,
                onResult = { instance ->
                    modelInstance = instance
                    if (instance == null) {
                        loadError = "模型实例化失败"
                    }
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            loadError = e.message ?: "模型加载失败"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        MiniChip(
            text = "返回模型文件夹",
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = model.modelName.ifBlank { "模型预览" },
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = modelUrl ?: "模型地址为空",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Scene(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                environment = environment,
                cameraManipulator = cameraManipulator
            ) {
                modelInstance?.let { instance ->
                    ModelNode(
                        modelInstance = instance,
                        scaleToUnits = 1.2f,
                        centerOrigin = Position(0.0f, 0.0f, 0.0f),
                        isEditable = true
                    )
                }

                // 主光：从前上方打下来
                LightNode(
                    type = LightManager.Type.DIRECTIONAL,
                    apply = {
                        color(1.0f, 1.0f, 1.0f)
                        intensity(120_000f)
                        direction(-0.6f, -1.0f, -0.8f)
                        castShadows(false)
                    }
                )

                // 背面补光
                LightNode(
                    type = LightManager.Type.DIRECTIONAL,
                    apply = {
                        color(1.0f, 1.0f, 1.0f)
                        intensity(70_000f)
                        direction(0.6f, -0.5f, 0.8f)
                        castShadows(false)
                    }
                )

                // 左侧补光
                LightNode(
                    type = LightManager.Type.DIRECTIONAL,
                    apply = {
                        color(1.0f, 1.0f, 1.0f)
                        intensity(55_000f)
                        direction(1.0f, -0.2f, 0.0f)
                        castShadows(false)
                    }
                )

                // 右侧补光
                LightNode(
                    type = LightManager.Type.DIRECTIONAL,
                    apply = {
                        color(1.0f, 1.0f, 1.0f)
                        intensity(55_000f)
                        direction(-1.0f, -0.2f, 0.0f)
                        castShadows(false)
                    }
                )

                // 底部补光：专门解决“底面发黑”
                LightNode(
                    type = LightManager.Type.DIRECTIONAL,
                    apply = {
                        color(1.0f, 1.0f, 1.0f)
                        intensity(45_000f)
                        direction(0.0f, 1.0f, 0.0f)
                        castShadows(false)
                    }
                )
            }

            when {
                modelUrl.isNullOrBlank() -> {
                    Text("模型地址为空", color = Color.White)
                }

                loadError != null -> {
                    Text(loadError ?: "模型加载失败", color = Color.White)
                }

                isLoading -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * 这个函数负责把模型地址转换为模拟器和本地环境都可访问的统一地址。
 */
fun normalizeModelUrl(rawUrl: String?): String? {
    val value = rawUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    return when {
        value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true) -> {
            value
                .replace("localhost", "10.0.2.2")
                .replace("127.0.0.1", "10.0.2.2")
//                .replace("localhost", "172.20.10.3")
//                .replace("127.0.0.1", "172.20.10.3")
        }

        value.startsWith("/") -> {
            "http://10.0.2.2:8080$value"
//            "http://172.20.10.3:8080$value"
        }

        else -> {
            "http://10.0.2.2:8080$value"
//            "http://172.20.10.3:8080/$value"
        }
    }
}
