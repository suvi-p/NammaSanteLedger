package com.example.nammasanthe.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object FullDashboard : Screen("full_dashboard")
    object Customers : Screen("customers")
    object CustomerProfile : Screen("customer_profile/{customerId}") {
        fun createRoute(customerId: Long) = "customer_profile/$customerId"
    }
    object AddTransaction : Screen("add_transaction/{customerId}/{type}") {
        fun createRoute(customerId: Long, type: String = "CREDIT") = "add_transaction/$customerId/$type"
    }
    object Overdue : Screen("overdue")
    object Menu : Screen("menu")
    object EditProfile : Screen("edit_profile")

    object History : Screen("history")
}
