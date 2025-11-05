package service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.DashboardStats
import model.Order
import model.OrderStatus

interface IOrderService {
    suspend fun createOrder(order: Order): Boolean
    suspend fun getAllOrders(userId: String): List<Order> // ⭐️ (THÊM)
    suspend fun getOrdersByStatus(userId: String, status: OrderStatus): List<Order> // ⭐️ (SỬA TÊN)
    suspend fun cancelOrder(orderId: String): Boolean // ⭐️ (THÊM)
    suspend fun updateOrder(order: Order): Boolean
    suspend fun getDashboardStatistics(): DashboardStats
    suspend fun getAllOrdersAdmin(): List<Order>
    suspend fun getOrdersByStatusAdmin(status: OrderStatus): List<Order>
}