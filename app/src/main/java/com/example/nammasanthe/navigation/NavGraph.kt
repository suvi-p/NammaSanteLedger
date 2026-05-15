package com.example.nammasanthe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nammasanthe.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.FullDashboard.route) {
            FullDashboardScreen(navController = navController)
        }

        composable(Screen.Customers.route) {
            CustomersScreen(navController = navController)
        }

        composable(
            route = Screen.CustomerProfile.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            CustomerProfileScreen(navController = navController, customerId = customerId)
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType; defaultValue = "CREDIT" }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            val type = backStackEntry.arguments?.getString("type") ?: "CREDIT"
            AddTransactionScreen(navController = navController, customerId = customerId, initialType = type)
        }

        composable(Screen.Overdue.route) {
            OverdueScreen(navController = navController)
        }

        composable(Screen.Menu.route) {
            MenuScreen(navController = navController)
        }

        // ✅ EditProfile route re-enabled for vendor profile editing
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController)
        }
    }
}
