package service

import model.Order.FeedBack

interface IFeedBackService {
    suspend fun createFeedBack(feedBack: FeedBack)
}