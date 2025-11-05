package repository.repositoryImplement

import android.util.Log
import com.google.firebase.Timestamp
import data.FirestoreBase
import repository.IFavoriteRepository

class FavoriteRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
) : IFavoriteRepository {

    // ⭐️ Đường dẫn đến sub-collection: "users/{userId}/favorites"
    private fun getCollectionPath(userId: String) = "users/$userId/favorites"

    /**
     * Kiểm tra bằng cách xem tài liệu có 'productId' đó có tồn tại không
     */
    override suspend fun isFavorite(userId: String, productId: String): Boolean {
        return try {
            val doc = firestore.getById(getCollectionPath(userId), productId)
            doc != null && doc.exists()
        } catch (e: Exception) { false }
    }

    /**
     * Thêm bằng cách tạo một tài liệu mới với ID là productId
     */
    override suspend fun addFavorite(userId: String, productId: String): Boolean {
        return try {
            // Chúng ta lưu 'addedAt' để biết khi nào, nhưng bạn có thể lưu 1 map rỗng
            val data = mapOf("addedAt" to Timestamp.now())
            // ⭐️ Dùng hàm 'updateData' (hoặc 'setData') thay vì 'addData'
            // để chúng ta có thể TỰ ĐẶT ID = productId
//            firestore.updateData(getCollectionPath(userId), productId, data)
            firestore.setData(getCollectionPath(userId), productId, data)
            true
        } catch (e: Exception) {
            Log.e("FavoriteRepository", "Lỗi thêm Favorite: ${e.message}", e)
            false
        }
    }

    /**
     * Xóa bằng cách xóa tài liệu có ID là productId
     */
    override suspend fun removeFavorite(userId: String, productId: String): Boolean {
        return try {
            firestore.deleteData(getCollectionPath(userId), productId)
            true
        } catch (e: Exception) {
            Log.e("FavoriteRepository", "Lỗi xóa Favorite: ${e.message}", e)
            false
        }
    }

    override suspend fun getAllFavoriteProductIds(userId: String): List<String> {
        return try {
            val docs = firestore.getAll(getCollectionPath(userId))
            docs.map { it.id } // 👈 Chỉ lấy ID (vì ID chính là productId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}