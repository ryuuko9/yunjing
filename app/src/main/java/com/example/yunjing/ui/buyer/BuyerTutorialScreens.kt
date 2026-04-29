package com.example.yunjing.ui.buyer

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yunjing.ui.pressClick

/**
 * 文件作用：
 * 承载 buyer 包中的教程相关页面，包括首页、教程列表、教程详情和若干局部卡片组件，
 * 让页面结构与导航外壳、个人页、工具函数分层清晰。
 */

/**
 * 买家首页
 */

/**
 * 作用：
 * 展示买家首页，负责快捷入口与最近导入教程摘要。
 */
@Composable
internal fun BuyerHomeScreen(
    onPrimaryScan: () -> Unit,
    onQuickAi: () -> Unit,
    onQuickTutorial: () -> Unit,
    recentTutorials: List<BuyerTutorialUi>,
    onOpenTutorial: (BuyerTutorialUi) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        Text(
            text = "云镜智联 · 买家",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "扫码导入商家发布项目，加入“我的教程”后可持续查看",
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
                    text = "开始安装",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "扫描商品二维码，将项目加入“我的教程”，并可查看爆炸图、打开教程播放器",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                PrimaryPillButton(
                    text = "扫码开始",
                    onClick = onPrimaryScan
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "AI 诊断",
                desc = "拍照/提问\n快速定位步骤",
                onClick = onQuickAi,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "我的教程",
                desc = "查看已导入教程\n继续学习",
                onClick = onQuickTutorial,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "最近导入",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))

        if (recentTutorials.isEmpty()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text("暂无教程", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "点击上方“扫码开始”，把商家发布的项目导入到“我的教程”。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    items(recentTutorials, key = { it.id }) { item ->
                        RecentRow(
                            title = item.tutorialName,
                            subtitle = item.addedAtText,
                            onClick = { onOpenTutorial(item) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * 作用：
 * 渲染首页快捷入口卡片。
 */
@Composable
private fun QuickActionCard(
    title: String,
    desc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(
        modifier = modifier,
        corner = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onClick)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * 作用：
 * 渲染首页“最近导入”区域中的单行摘要项。
 */
@Composable
private fun RecentRow(
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
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "›",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 其他 Tab
 */

/**
 * 作用：
 * 展示买家已导入教程列表和导入入口。
 */
@Composable
internal fun BuyerTutorialScreen(
    tutorials: List<BuyerTutorialUi>,
    onScanImport: () -> Unit,
    onPickLocalQrImage: () -> Unit,
    onOpenTutorial: (BuyerTutorialUi) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Text("我的教程", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "扫码导入后的项目会显示在这里，点击后可查看爆炸图和教程播放器",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        PrimaryPillButton(
            text = "扫码导入教程",
            onClick = onScanImport
        )

        Spacer(Modifier.height(10.dp))

        PrimaryPillButton(
            text = "从相册识别二维码",
            onClick = onPickLocalQrImage
        )

        Spacer(Modifier.height(16.dp))

        if (tutorials.isEmpty()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text("暂无已导入教程", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "商家发布后，买家扫码即可加入“我的教程”。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tutorials, key = { it.id }) { item ->
                    BuyerTutorialListRow(
                        item = item,
                        onClick = { onOpenTutorial(item) }
                    )
                }
            }
        }
    }
}

/**
 * 作用：
 * 渲染教程列表中的单个教程卡片。
 */
@Composable
private fun BuyerTutorialListRow(
    item: BuyerTutorialUi,
    onClick: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressClick(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.tutorialName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.projectDesc ?: "点击查看教程详情",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.addedAtText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "›",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 作用：
 * 展示教程详情页，包括爆炸图查看、播放器跳转和移除教程入口。
 */
@Composable
internal fun BuyerTutorialDetailScreen(
    tutorial: BuyerTutorialUi?,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var previewExploded by remember { mutableStateOf(false) }

    if (tutorial == null) {
        SimplePlaceholderPage(
            title = "教程详情",
            subtitle = "未找到教程内容"
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        PrimaryPillButton(
            text = "返回教程列表",
            onClick = onBack
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = tutorial.tutorialName,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = tutorial.projectDesc ?: "教程详情",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (!tutorial.explodedImageUrl.isNullOrBlank()) {
            BuyerExplodedImageCard(
                imageUrl = tutorial.explodedImageUrl,
                onPreview = { previewExploded = true }
            )
            Spacer(Modifier.height(14.dp))
        }

        if (!tutorial.tutorialVideoUrl.isNullOrBlank()) {
            BuyerTutorialPlayerCard(
                title = tutorial.tutorialTitle ?: "教程播放器",
                onOpenPlayer = { context.openBuyerTutorialPlayer(tutorial) }
            )
            Spacer(Modifier.height(14.dp))
        }

        DangerDeleteButton(
            text = "移除教程",
            onClick = onDelete
        )
    }

    if (previewExploded && !tutorial.explodedImageUrl.isNullOrBlank()) {
        BuyerExplodedImagePreviewDialog(
            imageUrl = tutorial.explodedImageUrl,
            onDismiss = { previewExploded = false }
        )
    }
}

/**
 * 作用：
 * 渲染教程详情中的爆炸图卡片。
 */
@Composable
private fun BuyerExplodedImageCard(
    imageUrl: String,
    onPreview: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("爆炸图", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = imageUrl,
                contentDescription = "爆炸图缩略图",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(12.dp))
            PrimaryPillButton(
                text = "查看爆炸图",
                onClick = onPreview
            )
        }
    }
}

/**
 * 作用：
 * 渲染教程详情中的播放器入口卡片。
 */
@Composable
private fun BuyerTutorialPlayerCard(
    title: String,
    onOpenPlayer: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "点击后可打开 Unity 教程播放器",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            PrimaryPillButton(
                text = "打开教程播放器",
                onClick = onOpenPlayer
            )
        }
    }
}

/**
 * 作用：
 * 在教程详情缺失时提供占位页面，避免页面直接空白。
 */
@Composable
private fun SimplePlaceholderPage(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(14.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(18.dp))
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text("页面搭好啦", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "后续把真实功能接进来：列表、搜索、筛选、拍照入口等。",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
