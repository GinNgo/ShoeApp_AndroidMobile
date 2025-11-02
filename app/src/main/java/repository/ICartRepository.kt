package repository

import model.Cart

interface ICartRepository{
    suspend fun createCartForUser(userId: String)
    suspend fun getCartByUserId(userId: String): Cart?
    suspend fun addProductInCart(cartId: String, productId: String)
    suspend fun deleteProductOutOfCart(cartId: String, productId: String)
}