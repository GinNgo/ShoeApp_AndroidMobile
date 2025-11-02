package repository

import model.Order.FeedBack

interface IFeedBackRepository {
    suspend fun createFeedBack(feedBack: FeedBack)
}