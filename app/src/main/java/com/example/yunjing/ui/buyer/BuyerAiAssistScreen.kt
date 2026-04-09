package com.example.yunjing.ui.buyer

import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.yunjing.R

data class BuyerAiMockVideo(
    val id: String,
    val title: String,
    val desc: String,
    @RawRes val resId: Int
)

fun buyerAiMockVideos(): List<BuyerAiMockVideo> {
    return listOf(
        BuyerAiMockVideo(
            id = "demo_1",
            title = "",
            desc = "安装过程演示",
            resId = R.raw.buyer_ai_demo_1
        ),
        BuyerAiMockVideo(
            id = "demo_2",
            title = "",
            desc = "安装过程演示",
            resId = R.raw.buyer_ai_demo_2
        ),
        BuyerAiMockVideo(
            id = "demo_3",
            title = "",
            desc = "安装过程演示",
            resId = R.raw.buyer_ai_demo_3
        )
    )
}

@Composable
fun BuyerAiRemoteAssistHome(
    onEnterAi: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF6F7FB),
                        Color(0xFFF2F4F8)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "远程协助",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101828)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "接入 AI 协助、接入人工协助",
            fontSize = 14.sp,
            color = Color(0xFF667085)
        )

        Spacer(modifier = Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "会话接入",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "建议：先接入 AI 辅助，不行再进行人工求助，效率最高",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF3366F0),
                                    Color(0xFF4F86FF)
                                )
                            )
                        )
                        .safeClick(onEnterAi)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "接入 AI 辅助",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun BuyerAiVideoSelectScreen(
    videos: List<BuyerAiMockVideo>,
    onBack: () -> Unit,
    onSelectVideo: (BuyerAiMockVideo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF6F7FB),
                        Color(0xFFF2F4F8)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .safeClick(onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "选择演示场景",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "请选择一个预置视频进入 AI 通话页",
                    fontSize = 13.sp,
                    color = Color(0xFF667085)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(videos, key = { it.id }) { item ->
                BuyerAiVideoCard(
                    video = item,
                    onClick = { onSelectVideo(item) }
                )
            }
        }
    }
}

@Composable
private fun BuyerAiVideoCard(
    video: BuyerAiMockVideo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .safeClick(onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = video.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = video.desc,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "进入演示",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun BuyerAiCallScreen(
    video: BuyerAiMockVideo,
    onExit: () -> Unit,
    onTransferHuman: () -> Unit
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var humanPressed by remember { mutableStateOf(false) }
    var shouldStopPlayer by remember { mutableStateOf(false) }

    BackHandler {
        shouldStopPlayer = true
        onExit()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        BuyerAiVideoPlayer(
            videoResId = video.resId,
            shouldStop = shouldStopPlayer,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.16f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.28f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI 辅助通话中",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = video.title,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 14.sp
            )
        }

        if (isMuted) {
            Text(
                text = "你已静音",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BuyerAiMuteActionButton(
                    muted = isMuted,
                    onClick = { isMuted = !isMuted }
                )

                BuyerAiPressableActionButton(
                    icon = Icons.Filled.HeadsetMic,
                    label = "转人工",
                    pressed = humanPressed,
                    onClick = {
                        humanPressed = !humanPressed
                        Toast.makeText(context, "正在为你转接人工协助", Toast.LENGTH_SHORT).show()
                        onTransferHuman()
                    }
                )

                BuyerAiExitActionButton(
                    onClick = {
                        shouldStopPlayer = true
                        onExit()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

        }
    }
}

@Composable
private fun BuyerAiMuteActionButton(
    muted: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (muted) {
        Color.White.copy(alpha = 0.92f)
    } else {
        Color(0xFFE6E1EA).copy(alpha = 0.82f)
    }

    val iconTint = if (muted) {
        Color.Red
    } else {
        Color(0xFF111111)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .safeClick(onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (muted) Icons.Filled.MicOff else Icons.Filled.Mic,
                contentDescription = "静音",
                tint = iconTint,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "静音",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun BuyerAiPressableActionButton(
    icon: ImageVector,
    label: String,
    pressed: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (pressed) {
        Color(0xFFD8D2DD).copy(alpha = 0.95f)
    } else {
        Color(0xFFE6E1EA).copy(alpha = 0.82f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .safeClick(onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF111111),
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun BuyerAiExitActionButton(
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Color(0xFFE6E1EA).copy(alpha = 0.82f))
                .safeClick(onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "退出",
                tint = Color.Red,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "退出",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun BuyerAiVideoPlayer(
    @RawRes videoResId: Int,
    shouldStop: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var player = remember(videoResId) {
        ExoPlayer.Builder(context).build().apply {
            val uri = RawResourceDataSource.buildRawResourceUri(videoResId)
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            volume = 0f
            prepare()
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(shouldStop) {
        if (shouldStop) {
            player.playWhenReady = false
            player.stop()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                setShutterBackgroundColor(android.graphics.Color.BLACK)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            view.player = player
        }
    )
}
@Composable
private fun Modifier.safeClick(onClick: () -> Unit): Modifier {
    return this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}