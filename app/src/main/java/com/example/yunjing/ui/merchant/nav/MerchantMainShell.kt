package com.example.yunjing.ui.merchant.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.yunjing.data.AuthStore
import com.example.yunjing.data.UserRole
import com.example.yunjing.nav.Destinations
import com.example.yunjing.ui.merchant.common.component.merchantSoftBackground
import com.example.yunjing.ui.merchant.content.repository.MerchantContentRepository
import com.example.yunjing.ui.merchant.content.viewmodel.MerchantContentViewModel
import com.example.yunjing.ui.merchant.content.viewmodel.MerchantContentViewModelFactory
import com.example.yunjing.ui.merchant.network.ApiProvider

/**
 * 本文件负责组装 merchant 端主壳层，统一初始化导航、账号信息与内容库 ViewModel。
 */

@Composable
fun MerchantMainShell(
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    /**
     * 这个函数负责初始化 merchant 主壳层依赖，并把导航与底部栏组装到同一个页面容器中。
     */
    val innerNav = rememberNavController()
    val context = LocalContext.current
    val authStore = remember(context) { AuthStore(context) }

    val api = remember {
        ApiProvider.merchantContentApi
    }

    val repository = remember(api) {
        MerchantContentRepository(api)
    }

    val factory = remember(repository) {
        MerchantContentViewModelFactory(repository)
    }

    val contentViewModel: MerchantContentViewModel = viewModel(
        factory = factory
    )

    val username by authStore
        .accountFlow(UserRole.MERCHANT)
        .collectAsState(initial = "未登录")

    val merchantUserId by authStore
        .userIdFlow(UserRole.MERCHANT)
        .collectAsState(initial = null)

    LaunchedEffect(merchantUserId) {
        contentViewModel.bindUser(merchantUserId)
    }

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
