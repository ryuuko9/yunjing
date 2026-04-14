package com.example.yunjing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.yunjing.R

private val PingFangSC = FontFamily(
    Font(R.font.pingfang_sc_ultralight, FontWeight.ExtraLight), // 200
    Font(R.font.pingfang_sc_thin, FontWeight.Thin),                      // 100
    Font(R.font.pingfang_sc_light, FontWeight.Light),                    // 300
    Font(R.font.pingfang_sc_regular, FontWeight.Normal),                 // 400
    Font(R.font.pingfang_sc_medium, FontWeight.Medium),                  // 500
    Font(R.font.pingfang_sc_semibold, FontWeight.SemiBold),              // 600
)

val Typography = Typography(
    // 标题层级（更 iOS）
    displayLarge = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // 正文/说明
    bodyLarge = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),

    // 标签/按钮文字
    labelLarge = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PingFangSC,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)