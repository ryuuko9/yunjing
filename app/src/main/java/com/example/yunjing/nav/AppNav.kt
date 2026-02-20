package com.example.yunjing.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.yunjing.data.AuthStore
import com.example.yunjing.data.RoleStore
import com.example.yunjing.data.SessionState
import com.example.yunjing.data.UserRole
import com.example.yunjing.ui.AuthScreen
import com.example.yunjing.ui.BuyerMainShell
import com.example.yunjing.ui.MerchantMainShell
import com.example.yunjing.ui.RoleSelectScreen
import kotlinx.coroutines.launch

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val ctx = LocalContext.current

    val roleStore = remember { RoleStore(ctx) }
    val authStore = remember { AuthStore(ctx) }
    val scope = rememberCoroutineScope()

    NavHost(navController = nav, startDestination = Destinations.GATE) {

        // 0) 启动分流：自动决定去 ROLE / AUTH / MAIN
        composable(Destinations.GATE) {
            val role by roleStore.roleFlow.collectAsState(initial = null)
            val session by authStore.sessionFlow.collectAsState(
                initial = SessionState(buyerLoggedIn = false, merchantLoggedIn = false)
            )

            // ✅ 防止 Flow 多次发射造成重复 navigate
            var didRoute by remember { mutableStateOf(false) }

            LaunchedEffect(role, session) {
                if (didRoute) return@LaunchedEffect

                val target = when (role) {
                    null -> Destinations.ROLE
                    UserRole.BUYER ->
                        if (session.buyerLoggedIn) Destinations.BUYER_MAIN else Destinations.auth(Destinations.ROLE_BUYER)
                    UserRole.MERCHANT ->
                        if (session.merchantLoggedIn) Destinations.MERCHANT_MAIN else Destinations.auth(Destinations.ROLE_MERCHANT)
                }

                didRoute = true
                nav.navigate(target) {
                    popUpTo(Destinations.GATE) { inclusive = true }
                    launchSingleTop = true
                }
            }

            // ✅ Gate 至少有 UI（避免黑屏/空白）
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // 1) 角色选择页
        composable(Destinations.ROLE) {
            RoleSelectScreen(
                onPickBuyer = {
                    scope.launch { roleStore.setRole(UserRole.BUYER) }
                    nav.navigate(Destinations.auth(Destinations.ROLE_BUYER)) {
                        launchSingleTop = true
                    }
                },
                onPickMerchant = {
                    scope.launch { roleStore.setRole(UserRole.MERCHANT) }
                    nav.navigate(Destinations.auth(Destinations.ROLE_MERCHANT)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 2) 登录/注册页
        composable(
            route = Destinations.AUTH_ROUTE,
            arguments = listOf(navArgument(Destinations.ARG_ROLE) { type = NavType.StringType })
        ) { entry ->
            val roleStr = entry.arguments?.getString(Destinations.ARG_ROLE).orEmpty()

            AuthScreen(
                role = roleStr,
                onBack = { nav.popBackStack() },
                onAuthSuccess = {
                    val target = if (roleStr == Destinations.ROLE_MERCHANT) {
                        Destinations.MERCHANT_MAIN
                    } else {
                        Destinations.BUYER_MAIN
                    }
                    nav.navigate(target) {
                        popUpTo(Destinations.AUTH_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 3) 买家主界面（加退出登录）
        composable(Destinations.BUYER_MAIN) {
            BuyerMainShell(
                onSwitchRole = {
                    scope.launch {
                        roleStore.clearRole()
                        nav.navigate(Destinations.ROLE) {
                            popUpTo(Destinations.BUYER_MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onLogout = {
                    scope.launch {
                        authStore.logout(UserRole.BUYER)
                        nav.navigate(Destinations.auth(Destinations.ROLE_BUYER)) {
                            popUpTo(Destinations.BUYER_MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // 4) 商家主界面（加退出登录）
        composable(Destinations.MERCHANT_MAIN) {
            MerchantMainShell(
                onSwitchRole = {
                    scope.launch {
                        roleStore.clearRole()
                        nav.navigate(Destinations.ROLE) {
                            popUpTo(Destinations.MERCHANT_MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onLogout = {
                    scope.launch {
                        authStore.logout(UserRole.MERCHANT)
                        nav.navigate(Destinations.ROLE) {
                            popUpTo(Destinations.MERCHANT_MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
