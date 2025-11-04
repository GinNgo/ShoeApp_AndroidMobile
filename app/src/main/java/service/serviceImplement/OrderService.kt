package service.serviceImplement

// ... (imports)
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Order
import model.OrderStatus
import repository.IOrderRepository
import repository.repositoryImplement.OrderRepository
import service.IOrderService

class OrderService(
    private val repository: IOrderRepository = OrderRepository()
) : IOrderService {

    override suspend fun createOrder(order: Order): Boolean = withContext(Dispatchers.IO) {
        repository.createOrder(order)
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
}