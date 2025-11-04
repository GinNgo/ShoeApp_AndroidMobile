package repository.repositoryImplement

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot // ⭐️ (THÊM)
import data.FirestoreBase
import model.FeedBack
import repository.IFeedBackRepository

class FeedBackRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "feedbacks"
) : IFeedBackRepository {

    /**
     * ⭐️ (MỚI) HÀM HELPER ĐỂ ĐỌC DỮ LIỆU
     * Chuyển DocumentSnapshot sang FeedBack model
     */
    private fun DocumentSnapshot.toFeedBack(): FeedBack? {
        return try {
            // 1. Dùng toObject() để map tự động
            // (Nó sẽ hoạt động vì model FeedBack.kt của bạn đã khớp với DB)
            val feedBack = this.toObject(FeedBack::class.java)
            Log.d("FeedBackRepository", "Parsed feedback: $feedBack")

            // 2. Dùng copy() để gán ID tài liệu vào model
            feedBack?.copy(id = this.id)
        } catch (e: Exception) {
            Log.e("FeedBackRepository", "Lỗi khi parse feedback: ${this.id}", e)
            null
        }
    }

    /**
     * ⭐️ (MỚI) HÀM HELPER ĐỂ GHI DỮ LIỆU
     * Chuyển FeedBack model sang HashMap
     */
    private fun FeedBack.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "userId" to this.userId,
            "productId" to this.productId,
            "orderId" to this.orderId,
            "rating" to this.rating,
            "review" to this.review,
            "createdAt" to (this.createdAt ?: Timestamp.now())
        )
    }

    /**
     * ⭐️ (SỬA) Dùng helper 'toHashMap'
     */
    override suspend fun createFeedBack(feedBack: FeedBack) {
        val data = feedBack.toHashMap() // 👈 Dùng helper
        firestore.addData(collectionName, data)
    }

    /**
     * ⭐️ (SỬA) Dùng helper 'toFeedBack'
     */
    override suspend fun getFeedbacksForProduct(productId: String): List<FeedBack> {
        return try {
            val docs = firestore.getListBy(collectionName, "productId", productId)
            Log.d("FeedBackRepository", "Fetched feedbacks: ${docs.size}")

            // 👈 Dùng helper
            docs.mapNotNull { doc ->
                doc.toFeedBack()
            }

        } catch (e: Exception) {
            Log.e("FeedBackRepository", "Lỗi getFeedbacks: ${e.message}", e)
            emptyList()
        }
    }
}