package service
import model.Order
import model.OrderStatus

interface IOrderService {
    suspend fun createOrder(order: Order): Boolean
    suspend fun getAllOrders(userId: String): List<Order> // ⭐️ (THÊM)
    suspend fun getOrdersByStatus(userId: String, status: OrderStatus): List<Order> // ⭐️ (SỬA TÊN)
    suspend fun cancelOrder(orderId: String): Boolean // ⭐️ (THÊM)
    suspend fun updateOrder(order: Order): Boolean

}