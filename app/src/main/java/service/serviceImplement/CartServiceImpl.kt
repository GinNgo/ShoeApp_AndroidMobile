package service.serviceImplement

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.CartItem
import repository.repositoryImplement.CartRepository

class CartService(private val repository: CartRepository = CartRepository()) {

    suspend fun getAllItems(userId: String): List<CartItem> = withContext(Dispatchers.IO) {
        repository.getAllItems(userId)
    }

    // ⭐️ ProductDetailActivity sẽ gọi hàm này
    suspend fun addItemToCart(item: CartItem): Boolean = withContext(Dispatchers.IO) {
        repository.addItem(item.userId, item)
    }

    suspend fun removeItemFromCart(userId: String, cartItemId: String): Boolean = withContext(Dispatchers.IO) {
        repository.removeItem(userId, cartItemId)
    }

    suspend fun updateItemQuantity(userId: String, cartItemId: String, newQuantity: Int): Boolean = withContext(Dispatchers.IO) {
        // ⭐️ Nếu số lượng <= 0, tự động xóa
        if (newQuantity <= 0) {
            return@withContext repository.removeItem(userId, cartItemId)
        }
        return@withContext repository.updateQuantity(userId, cartItemId, newQuantity)
    }
    suspend fun clearCart(userId: String): Boolean = withContext(Dispatchers.IO) {
        repository.clearCart(userId)
    }
}