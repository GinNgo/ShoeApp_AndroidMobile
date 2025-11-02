package repository

import model.Order.Order
import model.Order.OrderStatus

interface IOrderRepository {

    suspend fun createOrder(order: Order)

    suspend fun updateOrderStatus(orderId: String, orderStatus: OrderStatus)

    suspend fun getAllOrderByUserIdAndStatus(userId: String, status: OrderStatus) : List<Order>
}