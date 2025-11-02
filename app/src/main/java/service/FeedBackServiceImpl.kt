package service

import model.Order.FeedBack
import repository.IFeedBackRepository

data class FeedBackServiceImpl(
    private val feedBackRepository: IFeedBackRepository
): IFeedBackService{
    override suspend fun createFeedBack(feedBack: FeedBack) {
        // 🧩 Logic nghiệp vụ (nếu cần) – ví dụ: validate rating
        if (feedBack.rating < 1 || feedBack.rating > 5) {
            throw IllegalArgumentException("Rating must be between 1 and 5")
        }

        // 🟢 Lưu vào Firestore
        feedBackRepository.createFeedBack(feedBack)
    }
}
