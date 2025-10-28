package service

import model.Cart


interface ICartService {
    suspend fun createCartForUser(userId: String)
    suspend fun getCartByUserId(userId: String): Cart?
    suspend fun addProductToCart(userId: String, productId: String)
    suspend fun removeProductFromCart(userId: String, productId: String)
}