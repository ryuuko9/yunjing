package com.example.yunjing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

private enum class ChatRole { User, Assistant }

private data class ChatMsg(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val text: String,
)

@Composable
fun BuyerAiAssistScreen() {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember { mutableStateListOf<ChatMsg>() }

    var input by rememberSaveable { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val headerBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            MaterialTheme.colorScheme.background
        )
    )

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages += ChatMsg(
                role = ChatRole.Assistant,
                text = "你好，我是云镜智联 AI 助手。\n你可以描述卡点/报错现象，或点快捷问题开始。"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(headerBrush)
            .statusBarsPadding()
            .navigationBarsPadding() // 防止底部输入被 BottomBar 挡住
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(top = 10.dp, bottom = 14.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI 助手",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            QuickChipsRow(
                chips = listOf(
                    "安装到一半卡住了怎么办？",
                    "螺丝拧不进去/滑丝怎么办？",
                    "漏水/异响怎么排查？",
                    "怎么拍照更容易诊断？"
                ),
                onChipClick = { preset ->
                    if (!isThinking) input = preset
                }
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubble(msg)
                    }
                    if (isThinking) {
                        item(key = "thinking") { AssistantTypingBubble() }
                    }
                    item(key = "bottom_pad") { Spacer(Modifier.height(6.dp)) }
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AppTextField(
                                value = input,
                                onValueChange = { input = it },
                                placeholder = "描述问题："
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Box(modifier = Modifier.width(92.dp)) {
                            GradientButton(
                                text = "发送",
                                enabled = input.trim().isNotEmpty() && !isThinking,
                                onClick = {
                                    scope.launch {
                                        sendMessage(
                                            text = input,
                                            setInput = { input = it },
                                            addMsg = { messages += it },
                                            setThinking = { isThinking = it },
                                            scroll = {
                                                delay(60)
                                                val lastIndex = (messages.size + if (isThinking) 1 else 0).coerceAtLeast(0)
                                                if (lastIndex > 0) listState.animateScrollToItem(lastIndex - 1)
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📷 图片诊断",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.noIndicationClickable { }
                        )
                        Text(
                            text = "清空对话",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.noIndicationClickable {
                                if (!isThinking) {
                                    messages.clear()
                                    messages += ChatMsg(
                                        role = ChatRole.Assistant,
                                        text = "已清空。你可以重新描述问题，或点快捷问题开始。"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private suspend fun sendMessage(
    text: String,
    setInput: (String) -> Unit,
    addMsg: (ChatMsg) -> Unit,
    setThinking: (Boolean) -> Unit,
    scroll: suspend () -> Unit
) {
    val t = text.trim()
    if (t.isEmpty()) return

    setInput("")
    addMsg(ChatMsg(role = ChatRole.User, text = t))
    scroll()

    setThinking(true)
    delay(450)

    addMsg(ChatMsg(role = ChatRole.Assistant, text = fakeAiReply(t)))
    setThinking(false)
    scroll()
}

private fun fakeAiReply(userText: String): String {
    val t = userText.lowercase()
    return when {
        listOf("螺丝", "拧", "滑丝", "拧不进去").any { it in t } ->
            "先别硬拧，避免滑丝。\n\n1) 先“反向”轻拧找到牙口再正拧。\n2) 若孔位偏，先松开相邻固定点给余量再对齐。\n3) 若已滑丝：可换稍长螺丝或用螺纹修复方案。\n\n你现在卡在第几步？螺丝位置能描述一下吗？"

        listOf("漏水", "渗水").any { it in t } ->
            "漏水常见从接口密封/垫圈/方向三处排：\n\n1) 漏点近景 + 全局走管各一张。\n2) 垫圈是否缺失/装反，接口是否拧到位。\n3) 进出水方向是否接反。\n\n漏水是在接口处还是机身底部？"

        listOf("异响", "噪音", "吱", "嘎").any { it in t } ->
            "异响常见原因：未卡到底、摩擦碰擦、转动件偏心。\n\n1) 像摩擦还是敲击？\n2) 卡扣是否到位、线束是否扫到运动部件。\n3) 空载运行 10 秒，定位出现时机。\n\n异响在启动瞬间还是持续出现？"

        listOf("卡住", "对不上", "装不上").any { it in t } ->
            "“对不上/卡住”通常是方向装反或前置步骤没到位。\n\n建议：\n1) 回到上一步确认卡扣到位。\n2) 不要硬压，先松开周围 1~2 颗螺丝给余量。\n3) 看防呆结构：凸点/缺口是否对应。\n\n你把“第几步 + 哪两个零件对不上”说一下。"

        listOf("怎么拍", "拍照", "照片").any { it in t } ->
            "拍照建议：\n\n1) 全局一张（含问题位置）。\n2) 近景一张（对焦卡点/接口/螺丝孔）。\n3) 侧面一张（看装配深度）。\n4) 铭牌/型号也拍。\n\n后面我会给你接“图片诊断”入口。"

        else ->
            "我理解了。为了更快定位：\n\n1) 你在第几步遇到问题？\n2) 现象：卡住/对不上/漏水/异响/无法开机？\n3) 产品品类/型号是什么？\n\n你先补充“步骤 + 现象”，我给你排查路径。"
    }
}

@Composable
private fun ChatBubble(msg: ChatMsg) {
    val isUser = msg.role == ChatRole.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isUser) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            },
            tonalElevation = 0.dp
        ) {
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AssistantTypingBubble() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            tonalElevation = 0.dp
        ) {
            Text(
                text = "正在思考…",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickChipsRow(
    chips: List<String>,
    onChipClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Chip(text = chips.getOrNull(0).orEmpty(), onClick = { onChipClick(chips[0]) })
            Chip(text = chips.getOrNull(1).orEmpty(), onClick = { onChipClick(chips[1]) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Chip(text = chips.getOrNull(2).orEmpty(), onClick = { onChipClick(chips[2]) })
            Chip(text = chips.getOrNull(3).orEmpty(), onClick = { onChipClick(chips[3]) })
        }
    }
}

@Composable
private fun RowScope.Chip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(44.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .noIndicationClickable { onClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}