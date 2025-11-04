package service

import model.FeedBack
import model.Order

interface IFeedBackService {
    suspend fun createFeedBack(feedBack: FeedBack, orderToUpdate: Order): Boolean
    suspend fun getFeedbacksForProduct(productId: String): List<FeedBack>
}