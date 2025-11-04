package service.serviceImplement

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.FeedBack
import model.Order
import repository.IFeedBackRepository
import repository.IOrderRepository
import repository.repositoryImplement.FeedBackRepository
import repository.repositoryImplement.OrderRepository
import service.IFeedBackService
import service.IOrderService

/**
 * ⭐️ (SỬA) Đây là file Implementation đầy đủ, không phải data class
 */
class FeedBackService(
    // Inject (tiêm) 2 repository cần thiết
    private val feedBackRepository: IFeedBackRepository = FeedBackRepository(),
    private val orderRepository: IOrderRepository = OrderRepository()
): IFeedBackService {

    /**
     * ⭐️ (SỬA) Đây là logic đầy đủ cho hàm createFeedBack
     * (Thay thế cho TODO() hoặc NotImplementedError())
     */
    override suspend fun createFeedBack(feedBack: FeedBack, orderToUpdate: Order): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Validate (Kiểm tra)
            if (feedBack.rating < 1 || feedBack.rating > 5) {
                throw IllegalArgumentException("Rating phải từ 1 đến 5")
            }

            // 2. Tạo Feedback (Lưu vào collection 'feedbacks')
            feedBackRepository.createFeedBack(feedBack)

            // 3. Cập nhật Order (Lưu vào collection 'orders')
            // Tìm sản phẩm (item) trong đơn hàng
            val itemIndex = orderToUpdate.items.indexOfFirst { it.productId == feedBack.productId }

            if (itemIndex != -1) {
                // Tạo một list item mới
                val updatedItems = orderToUpdate.items.toMutableList()

                // Đánh dấu item này đã review
                updatedItems[itemIndex] = updatedItems[itemIndex].copy(isReviewed = true)

                // Tạo một object Order mới với list item đã cập nhật
                val updatedOrder = orderToUpdate.copy(items = updatedItems)

                // Gọi OrderRepository để ghi đè Order cũ
                orderRepository.updateOrder(updatedOrder)
            } else {
                Log.e("FeedBackService", "Không tìm thấy item ${feedBack.productId} trong order ${orderToUpdate.id}")
            }
            true // Báo thành công
        } catch (e: Exception) {
            Log.e("FeedBackService", "Lỗi tạo Feedback: ${e.message}", e)
            false // Báo thất bại
        }
    }
    override suspend fun getFeedbacksForProduct(productId: String): List<FeedBack> = withContext(Dispatchers.IO) {
        feedBackRepository.getFeedbacksForProduct(productId)
    }
}