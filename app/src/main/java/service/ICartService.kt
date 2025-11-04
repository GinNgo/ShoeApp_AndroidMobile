package service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Cart


interface ICartService {
    suspend fun createCartForUser(userId: String)
    suspend fun getCartByUserId(userId: String): Cart?
    suspend fun addProductToCart(userId: String, productId: String)
    suspend fun addProductToCart(userId: String, productId: String, quantity: Int)
    suspend fun removeProductFromCart(userId: String, productId: String)
    suspend fun clearCart(userId: String)
}