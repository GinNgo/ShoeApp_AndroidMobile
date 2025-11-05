package service.serviceImplement

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import model.Product
import repository.IFavoriteRepository
import repository.repositoryImplement.FavoriteRepository
import service.IFavoriteService
import service.serviceImplement.ProductService

class FavoriteService(
    private val favoriteRepo: IFavoriteRepository = FavoriteRepository(),
    private val productRepo: ProductService = ProductService() // ⭐️ Cần để lấy chi tiết SP
) : IFavoriteService {

    override suspend fun isFavorite(userId: String, productId: String): Boolean = withContext(Dispatchers.IO) {
        favoriteRepo.isFavorite(userId, productId)
    }

    override suspend fun addFavorite(userId: String, productId: String): Boolean = withContext(Dispatchers.IO) {
        favoriteRepo.addFavorite(userId, productId)
    }

    override suspend fun removeFavorite(userId: String, productId: String): Boolean = withContext(Dispatchers.IO) {
        favoriteRepo.removeFavorite(userId, productId)
    }

    /**
     * Lấy danh sách ID yêu thích, sau đó gọi ProductService để lấy chi tiết từng SP
     */
    override suspend fun getAllFavorites(userId: String): List<Product> = coroutineScope {
        withContext(Dispatchers.IO) {
            val productIds = favoriteRepo.getAllFavoriteProductIds(userId)

            // Tải song song chi tiết các sản phẩm
            val productJobs = productIds.map { id ->
                async { productRepo.getProductById(id) }
            }

            productJobs.mapNotNull { it.await() } // Chờ tất cả hoàn thành và lọc ra null
        }
    }
}