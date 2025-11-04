package repository.repositoryImplement

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import data.FirestoreBase // ⭐️ (THÊM)
import model.Product
import model.ProductColor
import model.ProductImage
import model.ProductSize
import repository.IProductRepository

// ⭐️ (SỬA) Implement Interface và Inject FirestoreBase
class ProductRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionPath: String = "products"
) : IProductRepository {

    // ⭐️ (SỬA) Hàm helper 'toProduct' đã đúng, giữ nguyên
    private fun DocumentSnapshot.toProduct(): Product? {
        try {
            val product = this.toObject(Product::class.java) ?: return null
            val colorsData = this.get("colors") as? List<Map<String, Any>> ?: emptyList()
            val parsedColors = colorsData.map { colorMap ->
                val hex = colorMap["hexCode"] as? String ?: "#000000"
                val name = colorMap["name"] as? String ?: "Không rõ"
                val sizesData = colorMap["sizes"] as? List<Map<String, Any>> ?: emptyList()
                val parsedSizes = sizesData.map { sizeMap ->
                    ProductSize(
                        size = sizeMap["size"] as? String ?: "N/A",
                        stockQuantity = (sizeMap["stockQuantity"] as? Long)?.toInt() ?: 0
                    )
                }
                ProductColor(hex, name, parsedSizes)
            }
            val imagesData = this.get("images") as? List<Map<String, Any>> ?: emptyList()
            val parsedImages = imagesData.map { imgMap ->
                ProductImage(
                    imageUrl = imgMap["imageUrl"] as? String ?: "",
                    isPrimary = imgMap["isPrimary"] as? Boolean ?: false
                )
            }
            return product.copy(
                id = this.id,
                colors = parsedColors,
                images = parsedImages
            )
        } catch (e: Exception) {
            Log.e("ProductRepository", "Lỗi khi phân tích (parse) product: ${this.id}", e)
            return null
        }
    }

    // ⭐️ (SỬA) Hàm helper 'toHashMap' đã đúng, giữ nguyên
    private fun Product.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "name" to this.name,
            "description" to this.description,
            "price" to this.price,
            "salePrice" to this.salePrice,
            "categoryIds" to this.categoryIds,
            "brandId" to this.brandId,
//            "material" to this.material,
//            "sizeChartUrl" to this.sizeChartUrl,
            "primaryImageUrl" to this.primaryImageUrl,
            "soldCount" to this.soldCount,
            "colors" to this.colors.map { color ->
                hashMapOf(
                    "hexCode" to color.hexCode,
                    "name" to color.name,
                    "sizes" to color.sizes.map { size ->
                        hashMapOf(
                            "size" to size.size,
                            "stockQuantity" to size.stockQuantity
                        )
                    }
                )
            },
            "images" to this.images.map { img ->
                hashMapOf(
                    "imageUrl" to img.imageUrl,
                    "isPrimary" to img.isPrimary
                )
            }
        )
    }

    override suspend fun getAllProducts(): List<Product> {
        val docs = firestore.getAll(collectionPath) // ⭐️ (SỬA)
        return docs.mapNotNull { it.toProduct() }
    }

    override suspend fun getSizeProduct(): Int {
        val docs = firestore.getAll(collectionPath) // ⭐️ (SỬA)
        return docs.size
    }

    override suspend fun addProduct(product: Product): Boolean {
        return try {
            val data = product.toHashMap()
            data["createdAt"] = (product.createdAt ?: Timestamp.now())
            firestore.addData(collectionPath, data) // ⭐️ (SỬA)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun getProductById(id: String): Product? {
        val doc = firestore.getById(collectionPath, id) // ⭐️ (SỬA)
        return doc?.toProduct()
    }

    override suspend fun updateProduct(product: Product): Boolean {
        return try {
            val data = product.toHashMap()
            firestore.updateData(collectionPath, product.id, data) // ⭐️ (SỬA)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun deleteProduct(id: String): Boolean {
        return try {
            firestore.deleteData(collectionPath, id) // ⭐️ (SỬA)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun getProductsByCategory(categoryId: String): List<Product> {
        // ⭐️ (SỬA) Dùng hàm 'getListByArrayContains' mới của FirestoreBase
        val docs = firestore.getListByArrayContains(collectionPath, "categoryIds", categoryId)
        return docs.mapNotNull { it.toProduct() }
    }
}