package service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Product
import repository.ProductRepository

class ProductService(
    private val repository: ProductRepository = ProductRepository()
) {

    // 🟢 Lấy tất cả sản phẩm
    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            repository.getAllProducts()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 🟢 Thêm sản phẩm
    suspend fun addProduct(product: Product): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.addProduct(product)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🟢 Lấy sản phẩm theo ID
    suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        try {
            repository.getProductById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🟢 Xóa sản phẩm
    suspend fun deleteProduct(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.deleteProduct(id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🟡 Lọc theo thương hiệu
    fun filterByBrand(products: List<Product>, brand: String): List<Product> {
        return products.filter { it.brand?.equals(brand, ignoreCase = true) == true }
    }

    // 🟡 Tính tổng giá
    fun calculateTotalPrice(product: Product, quantity: Int): Double {
        return product.price * quantity
    }

    // 🟡 Lấy ảnh chính (primary image)
    fun getPrimaryImageUrl(product: Product): String? {
        return product.images.firstOrNull { it.isPrimary }?.imageUrl
    }
}
