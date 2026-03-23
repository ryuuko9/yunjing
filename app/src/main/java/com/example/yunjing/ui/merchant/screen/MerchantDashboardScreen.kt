package com.example.yunjing.ui.merchant.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yunjing.ui.merchant.component.PrimaryPillButton
import com.example.yunjing.ui.merchant.component.QuickActionCard
import com.example.yunjing.ui.merchant.component.SoftCard
import com.example.yunjing.ui.merchant.model.MerchantQueueItem
import com.example.yunjing.ui.pressClick

// 工作台页面单独放

@Composable
fun MerchantDashboardScreen(
    onGoAssist: () -> Unit,
    onGoContent: () -> Unit
) {
    val queue = remember {
        listOf(
            MerchantQueueItem("待处理求助 · 3", "AI 已整理关键信息，建议优先处理"),
            MerchantQueueItem("进行中会话 · 1", "来自：智能门锁 · 远程标注中"),
            MerchantQueueItem("内容待发布 · 2", "说明书解析完成，等待检查版本")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Text("云镜智联 · 商家", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "处理求助、管理内容、发起远程协助",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("快速接入协助", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "一键进入会话，查看 AI 整理的步骤与画面",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                PrimaryPillButton(text = "进入远程协助", onClick = onGoAssist)
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "上传内容",
                desc = "说明书\n教程视频",
                icon = Icons.Filled.UploadFile,
                onClick = onGoContent,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "数据看板",
                desc = "成功率/耗时\n退货关联",
                icon = Icons.Filled.Analytics,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))
        Text("概览", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(queue) { item ->
                    DashboardRow(
                        title = item.title,
                        subtitle = item.subtitle,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardRow(
    title: String,
    subtitle: String,
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
        Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}