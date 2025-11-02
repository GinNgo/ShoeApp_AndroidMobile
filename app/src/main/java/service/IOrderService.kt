package service

import model.Order.Order
import model.Order.OrderStatus

interface IOrderService {
    suspend fun createOrder(order: Order)
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)

    suspend fun getAllOrderByUserIdAndStatus(userId: String, status: OrderStatus):List<Order>
}