package service

import model.OrderFullfillment

interface IOrderFullFillmentService {
    suspend fun createOrderFullFillment(orderFullfillment: OrderFullfillment)
}