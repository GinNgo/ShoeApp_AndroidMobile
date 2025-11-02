package repository

import model.Order.OrderFullfillment

interface IOrderFullFillmentRepository {
    suspend fun createOrderFullFillment(orderFullfillment: OrderFullfillment)
}