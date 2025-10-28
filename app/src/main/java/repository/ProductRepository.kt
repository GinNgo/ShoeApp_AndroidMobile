package repository

import com.google.firebase.Timestamp // ⭐️ Import Timestamp
import data.FirestoreBase
import kotlinx.coroutines.tasks.await
import model.Product

class ProductRepository(
    private val firestore: FirestoreBase = FirestoreBase(),
    private val collectionName: String = "products"
) {
    suspend fun getAllProducts(): List<Product> {
        val docs = firestore.getAll(collectionName)
        return docs.mapNotNull { doc ->
            val product = doc.toObject(Product::class.java)
            product?.copy(id = doc.id)
        }
    }

    suspend fun getSizeProduct(): Int {
        val docs = firestore.getAll(collectionName)
        return docs.size
    }

    /**
     * ⭐️ (VIẾT LẠI) Hàm helper để chuyển đổi Product sang HashMap,
     * xử lý đúng cấu trúc lồng nhau của Color và Size.
     */
    private fun Product.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "name" to this.name,
            "description" to this.description,
            "price" to this.price,
            // ⭐️ (ĐÃ XÓA) "stockQuantity" (đã chuyển vào ProductSize)
            "categoryIds" to this.categoryIds, // ⭐️ (THAY ĐỔI) từ categoryId
            "brand" to this.brand,
            "material" to this.material,
            "sizeChartUrl" to this.sizeChartUrl,
            "primaryImageUrl" to this.primaryImageUrl, // ⭐️ (BỔ SUNG) Cần cho update

            // ⭐️ (CẬP NHẬT) Cần map lồng nhau cho colors và sizes
            "colors" to this.colors.map { color ->
                hashMapOf(
                    "hexCode" to color.hexCode,
                    "name" to color.name,
                    // Map danh sách sizes bên trong
                    "sizes" to color.sizes.map { size ->
                        hashMapOf(
                            "size" to size.size,
                            "stockQuantity" to size.stockQuantity
                        )
                    }
                )
            },

            // ⭐️ (CẬP NHẬT) Giữ nguyên map cho images
            "images" to this.images.map { img ->
                hashMapOf(
                    "imageUrl" to img.imageUrl,
                    "isPrimary" to img.isPrimary
                )
            }
        )
    }

    /**
     * ⭐️ (SỬA LẠI) Dùng hàm helper toHashMap
     */
    suspend fun addProduct(product: Product) {
        // Bắt đầu với HashMap đã được map chuẩn
        val data = product.toHashMap()

        // Thêm trường createdAt chỉ khi tạo mới
        data["createdAt"] = (product.createdAt ?: Timestamp.now())

        firestore.addData(collectionName, data)
    }

    suspend fun getProductById(id: String): Product? {
        val doc = firestore.getById(collectionName, id)
        return doc?.toObject(Product::class.java)?.copy(id = doc.id)
    }

    /**
     * ⭐️ (SỬA LẠI) Dùng hàm helper toHashMap
     */
    suspend fun updateProduct(product: Product) {
        // Lấy HashMap đã map (không bao gồm createdAt)
        val data = product.toHashMap()

        // Firestore updateData sẽ chỉ cập nhật các trường có trong Map
        firestore.updateData(collectionName, product.id, data)
    }

    suspend fun deleteProduct(id: String) {
        firestore.deleteData(collectionName, id)
    }

    /**
     * ⭐️ (CẬP NHẬT) Sửa lại logic query theo danh sách categoryIds
     */
    suspend fun getProductsByCategory(categoryId: String): List<Product> {
        // ⚠️ QUAN TRỌNG:
        // Hàm getListBy của bạn BÂY GIỜ phải thực hiện query "array-contains"
        // chứ không phải "whereEqualTo" nữa.
        // Field truy vấn là "categoryIds" (số nhiều)
        val docs = firestore.getListBy(collectionName, "categoryIds", categoryId)

        return docs.mapNotNull { doc ->
            val product = doc.toObject(Product::class.java)
            product?.copy(id = doc.id)
        }
    }
}