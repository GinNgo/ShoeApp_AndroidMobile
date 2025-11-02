package service

import model.Order.OrderFullfillment

interface IOrderFullFillmentService {
    suspend fun createOrderFullFillment(orderFullfillment: OrderFullfillment)
}