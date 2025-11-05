package repository
import model.Order
import model.OrderStatus
import java.util.Date

interface IOrderRepository {
    suspend fun createOrder(order: Order): Boolean
    suspend fun getAllOrders(userId: String): List<Order> // ⭐️ (THÊM) Lấy tất cả
    suspend fun getOrdersByStatus(userId: String, status: OrderStatus): List<Order> // ⭐️ (SỬA TÊN)
    suspend fun cancelOrder(orderId: String): Boolean // ⭐️ (THÊM)
    suspend fun updateOrder(order: Order): Boolean
    // ⭐️ (THÊM MỚI) Lấy tất cả đơn hàng (không theo user)
    suspend fun getAllOrdersAdmin(): List<Order>

    // ⭐️ (THÊM MỚI) Lấy đơn hàng theo ngày
    suspend fun getOrdersByDateRange(startDate: Date, endDate: Date): List<Order>
    suspend fun getOrdersByStatusAdmin(status: OrderStatus): List<Order>
}