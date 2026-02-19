package com.example.yunjing.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yunjing.nav.Destinations

@Composable
fun MerchantMainShell(
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    var tab by remember { mutableStateOf(Destinations.MERCHANT_DASH) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = when (tab) {
                    Destinations.MERCHANT_DASH -> "商家 · 工作台"
                    Destinations.MERCHANT_CONTENT -> "商家 · 内容库"
                    Destinations.MERCHANT_ASSIST -> "商家 · 远程协助"
                    else -> "商家 · 我的"
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Destinations.MERCHANT_DASH,
                    onClick = { tab = Destinations.MERCHANT_DASH },
                    label = { Text("工作台") },
                    icon = { Text("📥") }
                )
                NavigationBarItem(
                    selected = tab == Destinations.MERCHANT_CONTENT,
                    onClick = { tab = Destinations.MERCHANT_CONTENT },
                    label = { Text("内容库") },
                    icon = { Text("🗂️") }
                )
                NavigationBarItem(
                    selected = tab == Destinations.MERCHANT_ASSIST,
                    onClick = { tab = Destinations.MERCHANT_ASSIST },
                    label = { Text("协助") },
                    icon = { Text("🎧") }
                )
                NavigationBarItem(
                    selected = tab == Destinations.MERCHANT_PROFILE,
                    onClick = { tab = Destinations.MERCHANT_PROFILE },
                    label = { Text("我的") },
                    icon = { Text("👤") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            Destinations.MERCHANT_DASH -> MerchantTabDashboard(Modifier.padding(padding))
            Destinations.MERCHANT_CONTENT -> MerchantTabContent(Modifier.padding(padding))
            Destinations.MERCHANT_ASSIST -> MerchantTabAssist(Modifier.padding(padding))
            Destinations.MERCHANT_PROFILE -> MerchantTabProfile(
                modifier = Modifier.padding(padding),
                onSwitchRole = onSwitchRole,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun MerchantTabDashboard(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("这里放：AI 转人工队列 / 待处理求助 / 最近会话", style = MaterialTheme.typography.titleMedium)
        Card { Column(Modifier.padding(16.dp)) { Text("下一步我们会把你原来的“工单列表/呼叫接入”接到这里。") } }
        Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
            Text("查看工单（下一步接入）")
        }
    }
}

@Composable
private fun MerchantTabContent(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("这里放：商品/说明书/PDF/模型/教程版本管理", style = MaterialTheme.typography.titleMedium)
        Card { Column(Modifier.padding(16.dp)) { Text("后面接：上传→生成管线→发布→版本校验。") } }
    }
}

@Composable
private fun MerchantTabAssist(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("这里放：进入远程协助会话、共享画面、接收标注（后续）", style = MaterialTheme.typography.titleMedium)
        Card { Column(Modifier.padding(16.dp)) { Text("后面接：WebRTC/房间、标注数据协议、叠加渲染。") } }
    }
}

@Composable
fun MerchantTabProfile(
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
        Text("团队与设置", style = MaterialTheme.typography.titleMedium)
        Card { Column(Modifier.padding(16.dp)) { Text("这里放：账号、团队、权限、帮助、隐私等。") } }

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
