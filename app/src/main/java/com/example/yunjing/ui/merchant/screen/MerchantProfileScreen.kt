package com.example.yunjing.ui.merchant.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.yunjing.ui.ProfileConfirmDialogs
import com.example.yunjing.ui.merchant.component.*

// "我的"
@Composable
fun MerchantProfileScreen(
    username: String,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    var showSwitchConfirm by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .merchantSoftBackground()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .padding(bottom = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("团队与设置", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(14.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MerchantAvatar(modifier = Modifier.size(84.dp))
                Spacer(Modifier.height(12.dp))
                Text(username, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "商家 · 团队账号",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileEntryCard(Icons.Filled.Settings, Color(0xFF28C76F), "通用设置") {}
            ProfileEntryCard(Icons.Filled.Security, Color(0xFF8B5CFF), "权限与安全") {}
            ProfileEntryCard(Icons.Filled.HelpOutline, Color(0xFFFFB020), "帮助与反馈") {}
            ProfileEntryCard(Icons.Filled.Info, Color(0xFF9AA4B2), "关于云镜智联") {}
        }

        Spacer(Modifier.height(16.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                ProfileActionRow("切换身份") { showSwitchConfirm = true }
                Spacer(Modifier.height(10.dp))
                DangerLogoutButton("退出登录") { showLogoutConfirm = true }
            }
        }
    }

    ProfileConfirmDialogs(
        showSwitchConfirm = showSwitchConfirm,
        showLogoutConfirm = showLogoutConfirm,
        onDismissSwitch = { showSwitchConfirm = false },
        onDismissLogout = { showLogoutConfirm = false },
        onConfirmSwitch = {
            showSwitchConfirm = false
            onSwitchRole()
        },
        onConfirmLogout = {
            showLogoutConfirm = false
            onLogout()
        },
        roleName = "商家",
        username = username
    )
}