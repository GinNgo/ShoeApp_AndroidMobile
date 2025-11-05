package service.serviceImplement

// ... (imports)
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.DashboardStats
import model.Order
import model.OrderStatus
import repository.IOrderRepository
import repository.repositoryImplement.OrderRepository
import service.IOrderService
import java.util.Calendar
import java.util.Date

class OrderService(
    private val repository: IOrderRepository = OrderRepository()
) : IOrderService {

    override suspend fun createOrder(order: Order): Boolean = withContext(Dispatchers.IO) {
        repository.createOrder(order)
    }
    override suspend fun getAllOrdersAdmin():  List<Order> = withContext(Dispatchers.IO) {
        repository.getAllOrdersAdmin()
    }
    // ⭐️ (THÊM)
    override suspend fun getAllOrders(userId: String): List<Order> = withContext(Dispatchers.IO) {
        repository.getAllOrders(userId)
    }

    // ⭐️ (SỬA TÊN)
    override suspend fun getOrdersByStatus(userId: String, status: OrderStatus): List<Order> = withContext(Dispatchers.IO) {
        repository.getOrdersByStatus(userId, status)
    }

    // ⭐️ (THÊM)
    override suspend fun cancelOrder(orderId: String): Boolean = withContext(Dispatchers.IO) {
        repository.cancelOrder(orderId)
    }
    override suspend fun updateOrder(order: Order): Boolean = withContext(Dispatchers.IO) {
        repository.updateOrder(order)
    }
    override suspend fun getOrdersByStatusAdmin(status: OrderStatus): List<Order> = withContext(Dispatchers.IO) {
        repository.getOrdersByStatusAdmin(status)
    }
    /**
     * ⭐️ (THÊM MỚI) Hàm tính toán thống kê cho Dashboard
     */
    override suspend fun getDashboardStatistics(): DashboardStats = withContext(Dispatchers.IO) {
        // 1. Lấy TẤT CẢ đơn hàng (để tính tổng)
        // ⭐️ (SỬA) Gọi hàm mới 'getAllOrdersAdmin'
        val allOrders = repository.getAllOrdersAdmin()

        // 2. Lấy đơn hàng HÔM NAY
        val (startOfDay, endOfDay) = getTodayDateRange()
        val todayOrders = repository.getOrdersByDateRange(startOfDay, endOfDay)

        // 3. Tính toán
        // Chỉ tính toán các đơn KHÔNG BỊ HỦY
        val validOrders = allOrders.filter { it.status != OrderStatus.CANCELLED }
        val validTodayOrders = todayOrders.filter { it.status != OrderStatus.CANCELLED }

        val totalOrders = validOrders.size
        val totalRevenue = validOrders.sumOf { it.totalAmount }

        val ordersToday = validTodayOrders.size
        val revenueToday = validTodayOrders.sumOf { it.totalAmount }

        return@withContext DashboardStats(
            totalOrders = totalOrders,
            ordersToday = ordersToday,
            totalRevenue = totalRevenue,
            revenueToday = revenueToday
        )
    }

    /**
     * ⭐️ (THÊM MỚI) Hàm helper lấy 00:00:00 và 23:59:59 của hôm nay
     */
    private fun getTodayDateRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()

        // Đặt về 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        // Đặt về 23:59:59
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time

        return Pair(startOfDay, endOfDay)
    }

}