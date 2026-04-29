package com.example.yunjing.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    logoResId: Int? = null
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp)
                    .padding(top = 30.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                    Spacer(Modifier.width(4.dp))
                }

                if (logoResId != null) {
                    Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = "team logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                Row(content = actions)
            }

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    enablePasswordToggle: Boolean = true
) {
    var showPassword by remember { mutableStateOf(false) }

    val visualTransformation: VisualTransformation =
        if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
            )
        },
        label = null,
        visualTransformation = visualTransformation,
        trailingIcon = {
            if (isPassword && enablePasswordToggle) {
                val icon = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                PressIconButton(
                    onClick = { showPassword = !showPassword },
                    size = 40.dp
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (showPassword) "隐藏密码" else "显示密码",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(51.dp),
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 15.sp,
            lineHeight = 18.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,

            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,

            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun Modifier.noIndicationClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = this.pressClick(
    enabled = enabled,
    onClick = onClick
)

@Composable
fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .pressClick(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
    }
}

@Composable
fun PressIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    pressedAlpha: Float = 0.55f,
    pressedScale: Float = 0.96f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .pressClick(
                enabled = enabled,
                pressedAlpha = pressedAlpha,
                pressedScale = pressedScale,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun Modifier.pressClick(
    enabled: Boolean = true,
    pressedAlpha: Float = 0.55f,
    pressedScale: Float = 0.96f,
    role: Role? = null,
    onClick: () -> Unit
): Modifier {
    var down by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (enabled && down) pressedAlpha else 1f,
        label = "pressAlphaFast"
    )
    val scale by animateFloatAsState(
        targetValue = if (enabled && down) pressedScale else 1f,
        label = "pressScaleFast"
    )

    return this
        .graphicsLayer {
            this.alpha = alpha
            scaleX = scale
            scaleY = scale
        }
        // 按下瞬间就把 down = true
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val anyDown = event.changes.any { it.changedToDownIgnoreConsumed() }
                    val anyUp = event.changes.any { it.changedToUpIgnoreConsumed() }

                    if (anyDown) down = true
                    if (anyUp) down = false
                }
            }
        }
        .clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = role,
            onClick = onClick
        )
}


@Composable
fun ProfileConfirmDialogs(
    showSwitchConfirm: Boolean,
    showLogoutConfirm: Boolean,
    onDismissSwitch: () -> Unit,
    onDismissLogout: () -> Unit,
    onConfirmSwitch: () -> Unit,
    onConfirmLogout: () -> Unit,
    roleName: String,
    username: String? = null
) {
    AppCenterDialog(
        visible = showSwitchConfirm,
        title = "切换身份",
        message = buildString {
            append("将返回身份选择页，你可以重新选择入口。")
            if (!username.isNullOrBlank()) append("\n\n当前账号：$username（$roleName）")
        },
        confirmText = "继续切换",
        cancelText = "取消",
        onConfirm = onConfirmSwitch,
        onCancel = onDismissSwitch,
        dismissOnClickOutside = false
    )

    AppCenterDialog(
        visible = showLogoutConfirm,
        title = "退出登录",
        message = buildString {
            append("退出后需要重新登录，确定要退出吗？")
            if (!username.isNullOrBlank()) append("\n\n当前账号：$username（$roleName）")
        },
        confirmText = "退出登录",
        cancelText = "取消",
        onConfirm = onConfirmLogout,
        onCancel = onDismissLogout,
        dismissOnClickOutside = false
    )
}

@Composable
fun AppCenterDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "确定",
    cancelText: String = "取消",
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = false
) {
    if (!visible) return

    Dialog(
        onDismissRequest = {
            if (dismissOnBackPress) onCancel()
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
            usePlatformDefaultWidth = false
        )
    ) {
        // 外面留白 + 居中小卡片
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 2.dp,
                shadowElevation = 18.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 280.dp, max = 320.dp)
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 取消（浅灰）
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .pressClick { onCancel() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cancelText,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        // 确认
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.95f))
                                .pressClick { onConfirm() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}