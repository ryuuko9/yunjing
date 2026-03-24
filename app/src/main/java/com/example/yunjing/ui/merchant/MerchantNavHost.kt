package com.example.yunjing.ui.merchant

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.yunjing.nav.Destinations
import com.example.yunjing.ui.merchant.screen.MerchantAssistScreen
import com.example.yunjing.ui.merchant.screen.MerchantContentScreen
import com.example.yunjing.ui.merchant.screen.MerchantDashboardScreen
import com.example.yunjing.ui.merchant.screen.MerchantProfileScreen
import com.example.yunjing.ui.merchant.viewmodel.MerchantContentViewModel

// 导航单独管理

@Composable
fun MerchantNavHost(
    nav: NavHostController,
    username: String,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    contentViewModel: MerchantContentViewModel
) {
    NavHost(
        navController = nav,
        startDestination = Destinations.MERCHANT_DASH
    ) {
        composable(Destinations.MERCHANT_DASH) {
            MerchantDashboardScreen(
                onGoAssist = { nav.navigate(Destinations.MERCHANT_ASSIST) },
                onGoContent = { nav.navigate(Destinations.MERCHANT_CONTENT) }
            )
        }
        composable(Destinations.MERCHANT_CONTENT) {
            MerchantContentScreen(viewModel = contentViewModel)
        }
        composable(Destinations.MERCHANT_ASSIST) {
            MerchantAssistScreen()
        }
        composable(Destinations.MERCHANT_PROFILE) {
            MerchantProfileScreen(
                username = username,
                onSwitchRole = onSwitchRole,
                onLogout = onLogout
            )
        }
    }
}