package repository

import com.google.firebase.Timestamp
import data.FirestoreBase
import model.Order.FeedBack

class FeedBackRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "feedbacks"
) : IFeedBackRepository {

    override suspend fun createFeedBack(feedBack: FeedBack) {
        val data = hashMapOf(
            "userId" to feedBack.userId,
            "productId" to feedBack.productId,
            "rating" to feedBack.rating,
            "review" to feedBack.review,
            "createdAt" to (feedBack.createAt ?: Timestamp.now())
        )
        firestore.addData(collectionName, data)
    }
}
