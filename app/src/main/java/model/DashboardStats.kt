package model

data class DashboardStats(
    val totalOrders: Int = 0,
    val ordersToday: Int = 0,
    val totalRevenue: Double = 0.0,
    val revenueToday: Double = 0.0
)