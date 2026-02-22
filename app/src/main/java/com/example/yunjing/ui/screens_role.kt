package com.example.yunjing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.yunjing.R
import androidx.compose.material3.Icon

@Composable
fun RoleSelectScreen(
    onPickBuyer: () -> Unit,
    onPickMerchant: () -> Unit
) {
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF6F8FF),
            Color(0xFFF8FAFF),
            Color(0xFFF7F7F8)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppTopBar(title = "云镜智联", logoResId = R.drawable.team_logo) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(top = 18.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 顶部文案层级（更 iOS）
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "选择入口",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "买家用于安装指导与问答；商家用于内容生产与远程协助。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))

            EntryCard(
                title = "买家版",
                subtitle = "扫码进入 · 拍照定位步骤 · AI 指引 · 一键转人工",
                chipText = "推荐",
                emoji = "🧩",
                iconBrush = Brush.linearGradient(
                    listOf(Color(0xFFE8F6FF), Color(0xFFDDEBFF))
                ),
                chipTint = Color(0xFF2C6CFF),
                onClick = onPickBuyer
            )

            EntryCard(
                title = "商家版",
                subtitle = "内容管理 · 生成 3D 步骤库 · 处理转人工工单",
                chipText = "创作",
                emoji = "🧰",
                iconBrush = Brush.linearGradient(
                    listOf(Color(0xFFFFF1E8), Color(0xFFFFE3D6))
                ),
                chipTint = Color(0xFFFF6A3D),
                onClick = onPickMerchant
            )

            Spacer(Modifier.height(10.dp))

            Spacer(Modifier.weight(1f))

            Text(
                text = "提示：身份可在「我的」里随时切换",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun EntryCard(
    title: String,
    subtitle: String,
    chipText: String,
    emoji: String,
    iconBrush: Brush,
    chipTint: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Surface(
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp, // 更克制，iOS 风
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .pressClick(
                pressedAlpha = 0.75f,
                pressedScale = 0.985f, // 微缩放更像 iOS
                onClick = onClick
            )
    ) {
        // 卡片内部“玻璃感/提亮”
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFFFFF), Color(0xFFF6F7FF))
                    )
                )
                .padding(horizontal = 18.dp, vertical = 22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // 顶部行：图标 + chip + 箭头
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 图标胶囊
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(iconBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                    }

                    Spacer(Modifier.width(14.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // 右侧小标签
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(chipTint.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = chipTint
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }

                // 副标题说明
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}