package com.example.yunjing.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment

import com.example.yunjing.R

@Composable
fun RoleSelectScreen(
    onPickBuyer: () -> Unit,
    onPickMerchant: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "云镜智联", logoResId = R.drawable.team_logo) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(start = 20.dp, end = 20.dp, top = 50.dp) // Increased top padding
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp) // Increased space between elements
        ) {
            Text("选择入口", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "买家用于安装指导与问答；商家用于内容生产与远程协助。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp)) // More space before the cards

            EntryCard(
                title = "买家版",
                subtitle = "扫码进入 · 拍照定位步骤 · AI 指引 · 一键转人工",
                badge = "Buyer",
                onClick = onPickBuyer
            )

            EntryCard(
                title = "商家版",
                subtitle = "内容管理 · 生成 3D 步骤库 · 处理转人工工单",
                badge = "Merchant",
                onClick = onPickMerchant
            )

            Spacer(Modifier.weight(1f))
            Text(
                "提示：身份可在“我的”里随时切换",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EntryCard(
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val cardHeight = 142.dp

    Surface(
        shape = shape,
        tonalElevation = 2.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clip(shape)
            .pressClick(
                pressedAlpha = 0.72f,
                pressedScale = 1f,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标块（你后续可换成真实 icon）
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (badge == "Buyer") "🧩" else "🧰",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}