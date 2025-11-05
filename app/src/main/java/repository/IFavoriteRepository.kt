package repository

import model.Product

interface IFavoriteRepository {
    /**
     * Kiểm tra xem một sản phẩm đã có trong danh sách yêu thích chưa
     */
    suspend fun isFavorite(userId: String, productId: String): Boolean

    /**
     * Thêm sản phẩm vào yêu thích
     */
    suspend fun addFavorite(userId: String, productId: String): Boolean

    /**
     * Xóa sản phẩm khỏi yêu thích
     */
    suspend fun removeFavorite(userId: String, productId: String): Boolean

    /**
     * Lấy danh sách ID của tất cả sản phẩm yêu thích
     */
    suspend fun getAllFavoriteProductIds(userId: String): List<String>
}