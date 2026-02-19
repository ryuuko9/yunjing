package com.example.yunjing.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yunjing.nav.Destinations

@Composable
fun BuyerMainShell(
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    var tab by remember { mutableStateOf(Destinations.BUYER_HOME) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = when (tab) {
                    Destinations.BUYER_HOME -> "买家 · 首页"
                    Destinations.BUYER_TUTORIAL -> "买家 · 教程"
                    Destinations.BUYER_AI -> "买家 · AI 帮助"
                    else -> "买家 · 我的"
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Destinations.BUYER_HOME,
                    onClick = { tab = Destinations.BUYER_HOME },
                    label = { Text("首页") },
                    icon = { Text("🏠") }
                )
                NavigationBarItem(
                    selected = tab == Destinations.BUYER_TUTORIAL,
                    onClick = { tab = Destinations.BUYER_TUTORIAL },
                    label = { Text("教程") },
                    icon = { Text("📘") }
                )
                NavigationBarItem(
                    selected = tab == Destinations.BUYER_AI,
                    onClick = { tab = Destinations.BUYER_AI },
                    label = { Text("AI") },
                    icon = { Text("✨") }
                )
                NavigationBarItem(
                    selected = tab == Destinations.BUYER_PROFILE,
                    onClick = { tab = Destinations.BUYER_PROFILE },
                    label = { Text("我的") },
                    icon = { Text("👤") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            Destinations.BUYER_HOME -> BuyerTabHome(Modifier.padding(padding))
            Destinations.BUYER_TUTORIAL -> BuyerTabTutorial(Modifier.padding(padding))
            Destinations.BUYER_AI -> BuyerTabAiHelp(Modifier.padding(padding))
            Destinations.BUYER_PROFILE -> BuyerTabProfile(
                modifier = Modifier.padding(padding),
                onSwitchRole = onSwitchRole,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun BuyerTabHome(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("这里放：扫码进入商品 / 最近一次教程 / 继续上次步骤", style = MaterialTheme.typography.titleMedium)
        Card { Column(Modifier.padding(16.dp)) { Text("下一步我们会把你原来的“扫码→商品→拍摄→步骤”接到这里。") } }
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("扫码进入（下一步接入）")
        }
    }
}

@Composable
private fun BuyerTabTutorial(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("这里放：步骤目录 + 3D/爆炸图说明书入口", style = MaterialTheme.typography.titleMedium)
        Card { Column(Modifier.padding(16.dp)) { Text("后面接：步骤卡片、关键件高亮、检查项、章节跳转。") } }
    }
}

@Composable
private fun BuyerTabAiHelp(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("这里放：拍照问 AI / 自动定位步骤 / 纠错建议", style = MaterialTheme.typography.titleMedium)
        Card { Column(Modifier.padding(16.dp)) { Text("后面接：CameraX + 上传 + AI 识别接口。") } }
    }
}

@Composable
private fun BuyerTabProfile(
    modifier: Modifier = Modifier,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("账号与设置", style = MaterialTheme.typography.titleMedium)
        Card { Column(Modifier.padding(16.dp)) { Text("这里放：设置、帮助、隐私、历史记录等。") } }

        OutlinedButton(onClick = onSwitchRole, modifier = Modifier.fillMaxWidth()) {
            Text("切换身份（回到买家/商家选择）")
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("退出登录", color = MaterialTheme.colorScheme.onError)
        }
    }
}
