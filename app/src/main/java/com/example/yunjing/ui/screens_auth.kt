package com.example.yunjing.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.yunjing.R
import androidx.compose.ui.platform.LocalContext
import com.example.yunjing.data.AuthStore
import com.example.yunjing.data.UserRole
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.yunjing.data.LegalText

enum class LegalDoc { TERMS, PRIVACY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    role: String, // 买家 or 商家
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit

) {
    val isBuyer = role != "merchant"

    var showLegalSheet by remember { mutableStateOf(false) }
    var legalDoc by remember { mutableStateOf(LegalDoc.TERMS) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val ctx = LocalContext.current
    val authStore = remember { AuthStore(ctx) }
    val scope = rememberCoroutineScope()

    // role string -> UserRole
    val userRole = if (role == "merchant") UserRole.MERCHANT else UserRole.BUYER

    var errorMsg by remember { mutableStateOf<String?>(null) }

    // 0=登录 1=注册
    var tab by rememberSaveable { mutableIntStateOf(0) }

    // 登录表单
    var loginAccount by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var loginAgree by rememberSaveable { mutableStateOf(false) }

    // 注册表单
    var regAccount by rememberSaveable { mutableStateOf("") }
    var regPwd by rememberSaveable { mutableStateOf("") }
    var regPwd2 by rememberSaveable { mutableStateOf("") }
    var regAgree by rememberSaveable { mutableStateOf(false) }

    // 注册成功弹窗
    var showRegisterSuccessDialog by remember { mutableStateOf(false) }

    val headerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
            MaterialTheme.colorScheme.background
        )
    )

    val clearRegisterFields = {
        regAccount = ""
        regPwd = ""
        regPwd2 = ""
        regAgree = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(headerBrush)
    ) {

        // 返回按钮
        BackButton(
            onBack = onBack,
            modifier = Modifier
                .zIndex(10f)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 10.dp)
        )

        // 整体可滚动：小屏不会裁剪
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 44.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(11.dp))

            Text(
                text = "Hello!",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "欢迎来到云镜智联",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(55.dp))

            Card(
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SegmentedTabs(
                        left = "账号登录",
                        right = if (isBuyer) "立即注册" else "商家注册",
                        selectedIndex = tab,
                        onSelect = {
                            errorMsg = null
                            tab = it
                        }
                    )

                    if (tab == 0) {
                        // =========================
                        // 登录
                        // =========================
                        Text("账号", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        AppTextField(
                            value = loginAccount,
                            onValueChange = { v -> loginAccount = v },
                            placeholder = "输入用户名/手机号/邮箱"
                        )

                        Text("密码", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        AppTextField(
                            value = loginPassword,
                            onValueChange = { v -> loginPassword = v },
                            placeholder = "输入密码（6-20 位）",
                            isPassword = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "没有账号？立即注册",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.noIndicationClickable {
                                    errorMsg = null
                                    loginPassword = ""
                                    tab = 1
                                }
                            )
                            Text(
                                "忘记密码",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // 登录页
                        CenterLogo()

                        errorMsg?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }

                        GradientButton(
                            text = "登 录",
                            enabled = loginAgree && loginAccount.isNotBlank() && loginPassword.isNotBlank(),
                            onClick = {
                                errorMsg = null
                                scope.launch {
                                    val ok = authStore.login(userRole, loginAccount.trim(), loginPassword)
                                    if (ok) onAuthSuccess() else errorMsg = "账号或密码错误"
                                }
                            }
                        )

                        AgreeRow(
                            checked = loginAgree,
                            onCheckedChange = { loginAgree = it },
                            onClickTerms = {
                                legalDoc = LegalDoc.TERMS
                                showLegalSheet = true
                            },
                            onClickPrivacy = {
                                legalDoc = LegalDoc.PRIVACY
                                showLegalSheet = true
                            }
                        )

                    } else {
                        // =========================
                        // 注册
                        // =========================

                        Text("账号", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        AppTextField(
                            value = regAccount,
                            onValueChange = { v -> regAccount = v },
                            placeholder = "用户名/手机号/邮箱"
                        )

                        Text("密码", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        AppTextField(
                            value = regPwd,
                            onValueChange = { v -> regPwd = v },
                            placeholder = "设置密码（6-20 位）",
                            isPassword = true
                        )

                        Text("确认密码", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        AppTextField(
                            value = regPwd2,
                            onValueChange = { v -> regPwd2 = v },
                            placeholder = "再次输入密码",
                            isPassword = true
                        )

                        val pwdOk = regPwd.length in 6..20
                        val pwdMatch = regPwd.isNotBlank() && regPwd == regPwd2

                        if (regPwd.isNotEmpty() && !pwdOk) {
                            Text(
                                "密码长度需为 6-20 位",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (regPwd2.isNotEmpty() && !pwdMatch) {
                            Text(
                                "两次密码不一致",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "已有账号？去登录",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.noIndicationClickable {
                                    errorMsg = null
                                    clearRegisterFields()
                                    tab = 0
                                }
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        CenterLogo()

                        errorMsg?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }

                        val clearRegisterFields = {
                            regAccount = ""
                            regPwd = ""
                            regPwd2 = ""
                            regAgree = false
                        }

                        GradientButton(
                            text = "注 册",
                            enabled = regAgree && regAccount.isNotBlank() && pwdOk && pwdMatch,
                            onClick = {
                                errorMsg = null
                                scope.launch {
                                    when {
                                        regPwd.length !in 6..20 -> { errorMsg = "密码长度需为 6-20 位"; return@launch }
                                        regPwd != regPwd2 -> { errorMsg = "两次密码不一致"; return@launch }
                                        else -> {
                                            val acc = regAccount.trim()
                                            authStore.register(userRole, acc, regPwd)
                                            // 预填登录账号（更顺手）
                                            loginAccount = acc
                                            // 可选：清空登录密码，要求重新输入
                                            loginPassword = ""
                                            // 可选：把勾选同步过去（不想同步就删掉这行）
                                            loginAgree = regAgree
                                            showRegisterSuccessDialog = true
                                        }
                                    }
                                }
                            }
                        )

                        AppCenterDialog(
                            visible = showRegisterSuccessDialog,
                            title = "注册成功",
                            message = "账号已创建，请使用刚才的账号密码登录。",
                            confirmText = "去登录",
                            cancelText = "稍后",
                            dismissOnClickOutside = false, // 再保险一次
                            onConfirm = {
                                showRegisterSuccessDialog = false
                                clearRegisterFields()
                                // 切换到登录页
                                tab = 0
                                // 清空登录密码，让用户重新输入更符合“再次登录”
                                loginPassword = ""
                            },
                            onCancel = {
                                clearRegisterFields()
                                showRegisterSuccessDialog = false
                            }
                        )

                        AgreeRow(
                            checked = regAgree,
                            onCheckedChange = { regAgree = it },
                            onClickTerms = {
                                legalDoc = LegalDoc.TERMS
                                showLegalSheet = true
                            },
                            onClickPrivacy = {
                                legalDoc = LegalDoc.PRIVACY
                                showLegalSheet = true
                            }
                        )


                    }
                }
            }
        }
        if (showLegalSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLegalSheet = false },
                sheetState = sheetState
            ) {
                LegalSheetContent(
                    title = if (legalDoc == LegalDoc.TERMS) "服务条款" else "隐私政策",
                    body = if (legalDoc == LegalDoc.TERMS) LegalText.Terms else LegalText.Privacy,
                )
            }
        }
    }
}

@Composable
private fun CenterLogo() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.team_logo),
            contentDescription = "team_logo",
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
private fun AgreeRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClickTerms: () -> Unit,
    onClickPrivacy: () -> Unit
) {
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        textDecoration = TextDecoration.Underline
    )

    val text = buildAnnotatedString {
        append("已阅读并同意")

        pushStringAnnotation(tag = "terms", annotation = "terms")
        withStyle(linkStyle) { append("《服务条款》") }
        pop()

        append("和")

        pushStringAnnotation(tag = "privacy", annotation = "privacy")
        withStyle(linkStyle) { append("《隐私政策》") }
        pop()
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)

        ClickableText(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            onClick = { offset ->
                text.getStringAnnotations(tag = "terms", start = offset, end = offset)
                    .firstOrNull()?.let { onClickTerms(); return@ClickableText }

                text.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                    .firstOrNull()?.let { onClickPrivacy(); return@ClickableText }
            }
        )
    }
}


@Composable
private fun SegmentedTabs(
    left: String,
    right: String,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            SegmentedItem(
                text = left,
                selected = selectedIndex == 0,
                onClick = { onSelect(0) },
                modifier = Modifier.weight(1f)
            )
            SegmentedItem(
                text = right,
                selected = selectedIndex == 1,
                onClick = { onSelect(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SegmentedItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Surface(
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .noIndicationClickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val brush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.95f else 0.35f),
            MaterialTheme.colorScheme.secondary.copy(alpha = if (enabled) 0.85f else 0.25f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(brush)
            .noIndicationClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun LegalSheetContent(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 只保留标题，不要右上角关闭
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp, max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 14.dp)
        ) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}