package com.example.yunjing.nav

object Destinations {
    const val GATE = "gate"
    // 首屏：身份选择
    const val ROLE = "role"
    const val ARG_ROLE = "role"
    const val ROLE_BUYER = "buyer"
    const val ROLE_MERCHANT = "merchant"
    const val AUTH = "auth"

    const val AUTH_ROUTE = "$AUTH/{$ARG_ROLE}"  // ✅ 带参数的完整 route


    // 两个主壳（底部导航）
    const val BUYER_MAIN = "buyer_main"
    const val MERCHANT_MAIN = "merchant_main"

    // Buyer Tabs
    const val BUYER_HOME = "buyer_home"
    const val BUYER_TUTORIAL = "buyer_tutorial"
    const val BUYER_AI = "buyer_ai"
    const val BUYER_PROFILE = "buyer_profile"

    // Merchant Tabs
    const val MERCHANT_DASH = "merchant_dash"
    const val MERCHANT_CONTENT = "merchant_content"
    const val MERCHANT_ASSIST = "merchant_assist"
    const val MERCHANT_PROFILE = "merchant_profile"

    // 你后续已有的深链路页面（先保留占位，后面再接回你之前那套）
    const val ARG_PRODUCT_ID = "productId"

    fun auth(role: String) = "$AUTH/$role"
}