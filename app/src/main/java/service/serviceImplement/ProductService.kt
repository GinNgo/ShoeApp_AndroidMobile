package service.serviceImplement

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Order
import model.Product
import repository.IProductRepository
import repository.repositoryImplement.ProductRepository


class ProductService(
    private val repository: IProductRepository = ProductRepository()
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
    suspend fun getSizeProduct(): Int = withContext(Dispatchers.IO) {
        try {
            repository.getSizeProduct()
        }
        catch (e: Exception) {
            e.printStackTrace()
            0
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
    suspend fun updateProduct(product: Product): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.updateProduct(product)
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
        return products.filter { it.brandId?.equals(brand, ignoreCase = true) == true }
    }

    fun filterByName(products: List<Product>, query: String): List<Product> {
        val normalizedQuery = query.trim().lowercase()
        return products.filter { it.name.lowercase().contains(normalizedQuery) == true }
    }

    // 🟡 Tính tổng giá
    fun calculateTotalPrice(product: Product, quantity: Int): Double {
        return product.price * quantity
    }

    // 🟡 Lấy ảnh chính (primary image)
    fun getPrimaryImageUrl(product: Product): String? {
        return product.images.firstOrNull { it.isPrimary }?.imageUrl
    }
    suspend fun getProductsByCategory(categoryId: String): List<Product> = withContext(Dispatchers.IO) {
        try {
            repository.getProductsByCategory(categoryId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    /**
     * ⭐️ (HÀM MỚI) Cập nhật tồn kho và số lượng đã bán
     * Hàm này sẽ đọc (Read) sản phẩm, sửa (Modify), và ghi (Write) lại.
     *
     * @param order Đơn hàng vừa tạo (hoặc vừa hủy)
     * @param isCancellation (false = Trừ kho, true = Cộng kho)
     */
    suspend fun updateStockForOrder(order: Order, isCancellation: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            // Lặp qua TẤT CẢ sản phẩm trong đơn hàng
            for (item in order.items) {
                // 1. Lấy (Read) sản phẩm hiện tại từ DB
                val product = repository.getProductById(item.productId)
                if (product == null) {
                    Log.e("ProductService", "Không tìm thấy SP ${item.productId} để cập nhật kho")
                    continue // Bỏ qua item này, tiếp tục item khác
                }

                // 2. Tính toán số lượng thay đổi
                // Nếu isCancellation = true (Hủy đơn), quantityChange = +SL (cộng trả kho)
                // Nếu isCancellation = false (Đặt hàng), quantityChange = -SL (trừ kho)
                val quantityChange = if (isCancellation) item.quantity else -item.quantity
                val soldChange = if (isCancellation) -item.quantity else item.quantity

                // 3. Cập nhật đối tượng Product trong code Kotlin
                val updatedColors = product.colors.map { color ->
                    // Nếu không phải màu này, giữ nguyên
                    if (color.name != item.selectedColor) {
                        color
                    } else {
                        // Nếu đúng màu, tìm và cập nhật size
                        val updatedSizes = color.sizes.map { size ->
                            if (size.size == item.selectedSize) {
                                // Cập nhật tồn kho
                                val newStock = size.stockQuantity + quantityChange
                                size.copy(stockQuantity = newStock)
                            } else {
                                size // Giữ nguyên size khác
                            }
                        }
                        color.copy(sizes = updatedSizes)
                    }
                }

                // 4. Tạo đối tượng product mới với 'colors' và 'soldCount' đã cập nhật
                val updatedProduct = product.copy(
                    colors = updatedColors,
                    soldCount = product.soldCount + soldChange
                )

                // 5. Ghi (Write) lại toàn bộ đối tượng Product vào DB
                repository.updateProduct(updatedProduct)
            }
            true // Hoàn thành
        } catch (e: Exception) {
            Log.e("ProductService", "Lỗi nghiêm trọng khi cập nhật kho: ${e.message}", e)
            false
        }
    }
}
