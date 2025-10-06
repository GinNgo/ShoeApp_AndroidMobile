package repository

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

    suspend fun addProduct(product: Product) {
        val data = hashMapOf(
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "stockQuantity" to product.stockQuantity,
            "createdAt" to (product.createdAt ?: com.google.firebase.Timestamp.now()),
            "categoryId" to product.categoryId,
            "brand" to product.brand,
            "material" to product.material,
            "color" to product.color,
            "sizeChartUrl" to product.sizeChartUrl,
            "images" to product.images.map {
                mapOf(
                    "imageUrl" to it.imageUrl,
                    "isPrimary" to it.isPrimary
                )
            }
        )
        firestore.addData(collectionName, data)
    }

    suspend fun getProductById(id: String): Product? {
        val doc = firestore.getById(collectionName, id)
        return doc?.toObject(Product::class.java)?.copy(id = doc.id)
    }

    suspend fun deleteProduct(id: String) {
        firestore.deleteData(collectionName, id)
    }
}
