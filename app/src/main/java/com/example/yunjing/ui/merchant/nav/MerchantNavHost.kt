package com.example.yunjing.ui.merchant.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.yunjing.nav.Destinations
import com.example.yunjing.ui.merchant.assist.screen.MerchantAssistScreen
import com.example.yunjing.ui.merchant.content.screen.MerchantContentScreen
import com.example.yunjing.ui.merchant.content.viewmodel.MerchantContentViewModel
import com.example.yunjing.ui.merchant.dashboard.screen.DashboardProjectItem
import com.example.yunjing.ui.merchant.dashboard.screen.MerchantDashboardScreen
import com.example.yunjing.ui.merchant.profile.screen.MerchantProfileScreen

/**
 * 本文件负责维护 merchant 端内部导航图，并把页面路由与共享 ViewModel 连接起来。
 */

@Composable
fun MerchantNavHost(
    nav: NavHostController,
    username: String,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    contentViewModel: MerchantContentViewModel
) {
    /**
     * 这个函数负责声明 merchant 端的页面路由，并在不同页面之间共享内容库状态。
     */
    NavHost(
        navController = nav,
        startDestination = Destinations.MERCHANT_DASH
    ) {
        composable(Destinations.MERCHANT_DASH) {
            val dashboardProjects = contentViewModel.projects.map { project ->
                DashboardProjectItem(
                    id = project.id,
                    projectName = project.projectName,
                    publishStatus = project.publishStatus,
                    parseStatus = project.parseStatus,
                    rebuildStatus = project.rebuildStatus,
                    hasRebuildOutput = project.hasRebuildOutput,
                    hasUploadedAssets = false
                )
            }

            MerchantDashboardScreen(
                onGoAssist = { nav.navigate(Destinations.MERCHANT_ASSIST) },
                onGoContent = { nav.navigate(Destinations.MERCHANT_CONTENT) },
                projects = dashboardProjects,
                onOpenProject = { projectId ->
                    nav.navigate("${Destinations.MERCHANT_CONTENT}/$projectId")
                }
            )
        }

        composable(Destinations.MERCHANT_CONTENT) {
            MerchantContentScreen(
                viewModel = contentViewModel,
                initialProjectId = null
            )
        }

        composable(
            route = "${Destinations.MERCHANT_CONTENT}/{projectId}",
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId")

            MerchantContentScreen(
                viewModel = contentViewModel,
                initialProjectId = projectId
            )
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
