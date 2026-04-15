package com.example.yunjing.ui.merchant.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yunjing.ui.ProfileConfirmDialogs
import com.example.yunjing.ui.merchant.common.component.MerchantAvatar
import com.example.yunjing.ui.merchant.common.component.SoftCard
import com.example.yunjing.ui.merchant.common.component.merchantSoftBackground
import com.example.yunjing.ui.merchant.profile.component.DangerLogoutButton
import com.example.yunjing.ui.merchant.profile.component.ProfileActionRow
import com.example.yunjing.ui.merchant.profile.component.ProfileEntryCard

/**
 * 本文件负责展示 merchant 端“我的”页面及账号相关操作入口。
 */
@Composable
fun MerchantProfileScreen(
    username: String,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    /**
     * 这个函数负责渲染 merchant 端个人页，并承接切换身份与退出登录操作。
     */
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
            ProfileEntryCard(Icons.AutoMirrored.Filled.HelpOutline, Color(0xFFFFB020), "帮助与反馈") {}
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
        onDismissSwitch = { },
        onDismissLogout = { },
        onConfirmSwitch = {
            onSwitchRole()
        },
        onConfirmLogout = {
            onLogout()
        },
        roleName = "商家",
        username = username
    )
}
