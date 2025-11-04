package repository

import model.CartItem

interface ICartRepository {
    suspend fun getAllItems(userId: String): List<CartItem>
    suspend fun addItem(userId: String, item: CartItem): Boolean
    suspend fun removeItem(userId: String, cartItemId: String): Boolean
    suspend fun updateQuantity(userId: String, cartItemId: String, newQuantity: Int): Boolean
    suspend fun clearCart(userId: String): Boolean
}