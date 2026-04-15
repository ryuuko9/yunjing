package com.example.yunjing.ui.merchant.assist.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yunjing.ui.merchant.common.component.PrimaryPillButton
import com.example.yunjing.ui.merchant.common.component.SoftCard

/**
 * 本文件负责展示 merchant 端的远程协助入口页。
 */

@Composable
fun MerchantAssistScreen() {
    /**
     * 这个函数负责渲染远程协助首页的说明文案与入口按钮。
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(14.dp))
        Text("远程协助", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "接入会话、查看 AI 步骤、发送标注",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("会话接入", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "建议：先接入 AI 已转人工的求助，效率最高",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                PrimaryPillButton(
                    text = "进入待处理队列",
                    onClick = {},
                )
            }
        }
    }
}
