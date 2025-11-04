package repository.repositoryImplement

import android.util.Log
import data.FirestoreBase // ⭐️ (THÊM)
import model.CartItem
import repository.ICartRepository // ⭐️ (THÊM)

// ⭐️ (SỬA) Implement Interface và Inject FirestoreBase
class CartRepository(
    private val firestore: FirestoreBase = FirestoreBase()
) : ICartRepository {

    // ⭐️ (SỬA) Hàm helper để lấy đường dẫn sub-collection
    private fun getCollectionPath(userId: String) = "users/$userId/cartItems"

    override suspend fun getAllItems(userId: String): List<CartItem> {
        return try {
            val snapshot = firestore.getAll(getCollectionPath(userId)) // ⭐️ (SỬA)
            snapshot.mapNotNull { doc ->
                doc.toObject(CartItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi lấy giỏ hàng: ${e.message}")
            emptyList()
        }
    }

    override suspend fun addItem(userId: String, item: CartItem): Boolean {
        return try {
            val collectionPath = getCollectionPath(userId)

            // ⭐️ (SỬA) Dùng hàm 'getDataWhere' của FirestoreBase
            val conditions = mapOf(
                "productId" to item.productId,
                "selectedColor" to item.selectedColor,
                "selectedSize" to item.selectedSize
            )
            val snapshot = firestore.getDataWhere(collectionPath, conditions)

            if (snapshot.isEmpty()) {
                firestore.addData(collectionPath, item.toHashMap()) // ⭐️ (SỬA)
            } else {
                val doc = snapshot.first()
                val existingItem = doc.toObject(CartItem::class.java)!!
                val newQuantity = existingItem.quantity + item.quantity
                firestore.updateData(collectionPath, doc.id, mapOf("quantity" to newQuantity)) // ⭐️ (SỬA)
            }
            true
        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi thêm giỏ hàng: ${e.message}", e)
            false
        }
    }

    override suspend fun removeItem(userId: String, cartItemId: String): Boolean {
        return try {
            firestore.deleteData(getCollectionPath(userId), cartItemId) // ⭐️ (SỬA)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateQuantity(userId: String, cartItemId: String, newQuantity: Int): Boolean {
        return try {
            firestore.updateData(getCollectionPath(userId), cartItemId, mapOf("quantity" to newQuantity)) // ⭐️ (SỬA)
            true
        } catch (e: Exception) { false }
    }

    // ⭐️ (THÊM) Hàm helper toHashMap cho CartItem
    private fun CartItem.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "userId" to userId,
            "productId" to productId,
            "productName" to productName,
            "productImage" to productImage,
            "selectedColor" to selectedColor,
            "selectedSize" to selectedSize,
            "quantity" to quantity,
            "price" to price,
            "salePrice" to salePrice
//            "unitPrice" to unitPrice
        )
    }
    override suspend fun clearCart(userId: String): Boolean {
        return try {
            val allItems = getAllItems(userId)
            if (allItems.isEmpty()) return true

            firestore.runBatch { batch ->
                val collectionPath = getCollectionPath(userId)
                for (item in allItems) {
                    val docRef = firestore.getDocRef(collectionPath, item.id)
                    batch.delete(docRef)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi xóa giỏ hàng: ${e.message}")
            false
        }
    }
}