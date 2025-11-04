package repository

import model.OrderFullfillment

interface IOrderFullFillmentRepository {
    suspend fun createOrderFullFillment(orderFullfillment: OrderFullfillment)
}