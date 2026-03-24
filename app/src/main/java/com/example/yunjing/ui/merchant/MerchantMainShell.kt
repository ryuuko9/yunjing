package com.example.yunjing.ui.merchant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.yunjing.data.AuthStore
import com.example.yunjing.data.UserRole
import com.example.yunjing.nav.Destinations
import com.example.yunjing.ui.merchant.component.MerchantBottomBar
import com.example.yunjing.ui.merchant.component.merchantSoftBackground
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yunjing.ui.merchant.viewmodel.MerchantContentViewModel

// 只保留主壳，不再放 NavHost 和底部栏实现

@Composable
fun MerchantMainShell(
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    val innerNav = rememberNavController()
    val contentViewModel: MerchantContentViewModel = viewModel()
    val context = LocalContext.current
    val authStore = remember(context) { AuthStore(context) }

    val username by authStore
        .accountFlow(UserRole.MERCHANT)
        .collectAsState(initial = "未登录")

    val navBackStackEntry by innerNav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxSize()
            .merchantSoftBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                MerchantNavHost(
                    nav = innerNav,
                    username = username,
                    onSwitchRole = onSwitchRole,
                    onLogout = onLogout,
                    contentViewModel = contentViewModel
                )
            }

            MerchantBottomBar(
                currentDestination = currentDestination,
                onTabClick = { route ->
                    if (route == Destinations.MERCHANT_DASH) {
                        innerNav.popBackStack(Destinations.MERCHANT_DASH, inclusive = false)
                    } else {
                        innerNav.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Destinations.MERCHANT_DASH) { saveState = true }
                        }
                    }
                }
            )
        }
    }
}