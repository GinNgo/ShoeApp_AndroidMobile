package repository

import com.google.firebase.Timestamp
import data.FirestoreBase
import model.Cart

class CartRepositoryImpl(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "carts"
) : ICartRepository {

    override suspend fun createCartForUser(userId: String) {
        val data = hashMapOf(
            "userId" to userId,
            "createdAt" to Timestamp.now(),
            "products" to emptyMap<String, Int>(),
            "exist" to true
        )
        firestore.addData(collectionName, data)
    }

    // 🟢 2. Lấy giỏ hàng theo userId
    override suspend fun getCartByUserId(userId: String): Cart? {
        val docs = firestore.getAll(collectionName)
        val doc = docs.find { it.getString("userId") == userId }
        return doc?.toObject(Cart::class.java)?.copy(id = doc.id)
    }

    override suspend fun addProductInCart(cartId: String, productId: String) {
        val doc = firestore.getById(collectionName, cartId)
        if (doc != null && doc.exists()) {
            val cart = doc.toObject(Cart::class.java)
            val updatedProducts = cart?.products?.toMutableMap() ?: mutableMapOf()

            // ✅ Nếu đã có sản phẩm → tăng số lượng, nếu chưa có → thêm 1
            updatedProducts[productId] = (updatedProducts[productId] ?: 0) + 1

            firestore.updateData(collectionName, cartId, mapOf("products" to updatedProducts))
        }
    }


    override suspend fun deleteProductOutOfCart(cartId: String, productId: String) {
        val doc = firestore.getById(collectionName, cartId)
        if (doc != null && doc.exists()) {
            val cart = doc.toObject(Cart::class.java)
            val updatedProducts = cart?.products?.toMutableMap() ?: mutableMapOf()

            updatedProducts[productId]?.let { quantity ->
                // xóa hoàn toàn sản phẩm
                updatedProducts.remove(productId)
            }

            firestore.updateData(collectionName, cartId, mapOf("products" to updatedProducts))
        }
    }
}