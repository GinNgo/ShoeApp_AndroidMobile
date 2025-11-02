package service

import model.Order.OrderFullfillment
import repository.IOrderFullFillmentRepository

data class OrderFullFillmentServiceImpl(
    private val orderFullFillmentRepository: IOrderFullFillmentRepository
) : IOrderFullFillmentService {
    override suspend fun createOrderFullFillment(orderFullfillment: OrderFullfillment) {
        // Có thể thêm logic kiểm tra đơn hàng hợp lệ, v.v.
        orderFullFillmentRepository.createOrderFullFillment(orderFullfillment)
    }
}
