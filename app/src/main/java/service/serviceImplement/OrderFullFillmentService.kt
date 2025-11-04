package service.serviceImplement

import model.OrderFullfillment
import repository.IOrderFullFillmentRepository
import service.IOrderFullFillmentService

data class OrderFullFillmentService(
    private val orderFullFillmentRepository: IOrderFullFillmentRepository
) : IOrderFullFillmentService {
    override suspend fun createOrderFullFillment(orderFullfillment: OrderFullfillment) {
        // Có thể thêm logic kiểm tra đơn hàng hợp lệ, v.v.
        orderFullFillmentRepository.createOrderFullFillment(orderFullfillment)
    }
}
