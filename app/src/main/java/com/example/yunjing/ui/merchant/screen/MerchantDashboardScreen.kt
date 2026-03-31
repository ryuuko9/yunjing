package com.example.yunjing.ui.merchant.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yunjing.ui.merchant.component.PrimaryPillButton
import com.example.yunjing.ui.merchant.component.QuickActionCard
import com.example.yunjing.ui.merchant.component.SoftCard
import com.example.yunjing.ui.pressClick

@Composable
fun MerchantDashboardScreen(
    onGoAssist: () -> Unit,
    onGoContent: () -> Unit,
    projects: List<DashboardProjectItem>,
    onOpenProject: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        Text(
            text = "云镜智联 · 商家",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "处理求助、管理内容、发起远程协助",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "快速接入协助",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "一键进入会话，查看 AI 整理的步骤与画面",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(14.dp))

                PrimaryPillButton(
                    text = "进入远程协助",
                    onClick = onGoAssist
                )
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

        Text(
            text = "项目概览",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(10.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无项目，先去内容库创建一个吧",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = projects,
                        key = { it.id }
                    ) { project ->
                        DashboardProjectCard(
                            project = project,
                            onClick = { onOpenProject(project.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardProjectCard(
    project: DashboardProjectItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .pressClick(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = project.projectName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = buildProjectStatusText(project),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(12.dp))

        PublishBadge(
            text = if (project.publishStatus == "PUBLISHED") "已发布" else "未发布"
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = "›",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PublishBadge(
    text: String
) {
    val isPublished = text == "已发布"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (isPublished) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isPublished) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun buildProjectStatusText(project: DashboardProjectItem): String {
    return when {
        project.publishStatus == "PUBLISHED" -> "点击进入项目工作台 · 已发布"
        project.parseStatus == "COMPLETED" -> "点击进入项目工作台 · 解析完成，等待发布"
        project.hasRebuildOutput == 1 || project.rebuildStatus == "COMPLETED" -> "点击进入项目工作台 · 已重建，等待解析"
        project.hasUploadedAssets -> "点击进入项目工作台 · 已上传，等待重建"
        else -> "点击进入项目工作台 · 等待上传素材"
    }
}

data class DashboardProjectItem(
    val id: Long,
    val projectName: String,
    val publishStatus: String? = null,
    val parseStatus: String? = null,
    val rebuildStatus: String? = null,
    val hasRebuildOutput: Int? = null,
    val hasUploadedAssets: Boolean = false
)