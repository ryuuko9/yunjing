package com.example.yunjing.ui.merchant

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.yunjing.nav.Destinations
import com.example.yunjing.ui.merchant.screen.DashboardProjectItem
import com.example.yunjing.ui.merchant.screen.MerchantAssistScreen
import com.example.yunjing.ui.merchant.screen.MerchantContentScreen
import com.example.yunjing.ui.merchant.screen.MerchantDashboardScreen
import com.example.yunjing.ui.merchant.screen.MerchantProfileScreen
import com.example.yunjing.ui.merchant.viewmodel.MerchantContentViewModel

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