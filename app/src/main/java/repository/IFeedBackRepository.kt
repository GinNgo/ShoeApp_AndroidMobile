package repository

import model.FeedBack

interface IFeedBackRepository {
    suspend fun createFeedBack(feedBack: FeedBack)
    suspend fun getFeedbacksForProduct(productId: String): List<FeedBack>
}