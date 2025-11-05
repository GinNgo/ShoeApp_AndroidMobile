package service

import model.Product

interface IFavoriteService {
    suspend fun isFavorite(userId: String, productId: String): Boolean
    suspend fun addFavorite(userId: String, productId: String): Boolean
    suspend fun removeFavorite(userId: String, productId: String): Boolean

    // ⭐️ Hàm này sẽ trả về List<Product> đầy đủ (để dùng cho màn hình Wishlist)
    suspend fun getAllFavorites(userId: String): List<Product>
}