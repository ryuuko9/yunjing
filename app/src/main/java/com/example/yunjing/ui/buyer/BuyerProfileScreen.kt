package com.example.yunjing.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yunjing.ui.ProfileConfirmDialogs
import com.example.yunjing.ui.pressClick

/**
 * 文件作用：
 * 承载买家端“我的账号”页面及其专属的入口卡片、头像区和账号操作区，
 * 将个人页相关结构与教程页、导航壳体彻底分离。
 */

private val EmptyAction: () -> Unit = {}

/**
 * 作用：
 * 描述个人页中的一个功能入口项，包括图标、颜色、标题和点击行为。
 */
private data class BuyerProfileEntry(
    val icon: ImageVector,
    val iconBg: Color,
    val title: String,
    val onClick: () -> Unit
)

private val BuyerProfileEntries = listOf(
    BuyerProfileEntry(
        icon = Icons.Filled.Settings,
        iconBg = Color(0xFF28C76F),
        title = "通用设置",
        onClick = EmptyAction // TODO: 设置页
    ),
    BuyerProfileEntry(
        icon = Icons.Filled.Person,
        iconBg = Color(0xFF4BB3FF),
        title = "个人信息",
        onClick = EmptyAction // TODO: 个人资料页
    ),
    BuyerProfileEntry(
        icon = Icons.Filled.Security,
        iconBg = Color(0xFF8B5CFF),
        title = "安全选项",
        onClick = EmptyAction // TODO: 安全设置页
    ),
    BuyerProfileEntry(
        icon = Icons.Filled.HelpOutline,
        iconBg = Color(0xFFFFB020),
        title = "帮助与反馈",
        onClick = EmptyAction // TODO: 反馈/客服
    ),
    BuyerProfileEntry(
        icon = Icons.Filled.Info,
        iconBg = Color(0xFF9AA4B2),
        title = "关于云镜智联",
        onClick = EmptyAction // TODO: 关于页
    )
)

/**
 * 作用：
 * 展示买家个人页，包括头像信息、功能入口和切换身份/退出登录操作。
 */
@Composable
fun BuyerProfileScreen(
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
            .buyerSoftBackground()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .padding(bottom = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // 顶部标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "我的账号",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(14.dp))

        // 头像 + 昵称区
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BuyerAvatar(
                    modifier = Modifier.size(84.dp)
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    text = username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "买家 · 未绑定手机号",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 功能入口列表
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BuyerProfileEntries.forEach { entry ->
                ProfileEntryCard(
                    icon = entry.icon,
                    iconBg = entry.iconBg,
                    title = entry.title,
                    onClick = entry.onClick
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 账号操作区（切换身份/退出登录）
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                ProfileActionRow(
                    text = "切换身份",
                    onClick = { showSwitchConfirm = true }
                )
                Spacer(Modifier.height(10.dp))
                DangerLogoutButton(
                    text = "退出登录",
                    onClick = { showLogoutConfirm = true }
                )
            }
        }
    }

    // 禁止点空白关闭（防误触）
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
        roleName = "买家",
        username = username
    )
}

/**
 * 作用：
 * 渲染个人页顶部的头像占位视觉。
 */
@Composable
private fun BuyerAvatar(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "云",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 作用：
 * 渲染个人页中单个功能入口卡片。
 */
@Composable
private fun ProfileEntryCard(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pressClick(onClick = onClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = shape
            ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg.copy(alpha = 0.90f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 作用：
 * 渲染“切换身份”这类主操作行。
 */
@Composable
private fun ProfileActionRow(
    text: String, // 按钮文字
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary) // 可自定义颜色
            .pressClick(onClick = onClick) // 保留原有点击效果
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center // 内容居中
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary // 文字颜色
        )
    }
}

/**
 * 作用：
 * 渲染“退出登录”危险操作按钮。
 */
@Composable
private fun DangerLogoutButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(Color(0xFFFF3B30).copy(alpha = 0.92f))
            .pressClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
