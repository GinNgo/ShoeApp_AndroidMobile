package repository
import model.Order
import model.OrderStatus

interface IOrderRepository {
    suspend fun createOrder(order: Order): Boolean
    suspend fun getAllOrders(userId: String): List<Order> // ⭐️ (THÊM) Lấy tất cả
    suspend fun getOrdersByStatus(userId: String, status: OrderStatus): List<Order> // ⭐️ (SỬA TÊN)
    suspend fun cancelOrder(orderId: String): Boolean // ⭐️ (THÊM)
    suspend fun updateOrder(order: Order): Boolean
}