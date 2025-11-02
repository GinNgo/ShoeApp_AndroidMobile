package service

import model.Order.Order
import model.Order.OrderStatus
import repository.IOrderRepository
import repository.OrderRepository

class OrderServiceImpl(
    private val orderRepository: IOrderRepository = OrderRepository()
): IOrderService {
    override suspend fun createOrder(order: Order) {
        // 🧩 Logic nghiệp vụ: ví dụ kiểm tra số lượng, validate user
        if (order.quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than 0")
        }

        // 🟢 Gọi repository để lưu
        orderRepository.createOrder(order)
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus
    ) {
        // 🟢 Gọi repository để update trạng thái
        orderRepository.updateOrderStatus(orderId, status)
    }

    override suspend fun getAllOrderByUserIdAndStatus(
        userId: String,
        status: OrderStatus
    ):List<Order> {
      return orderRepository.getAllOrderByUserIdAndStatus(userId, status)
    }

}
