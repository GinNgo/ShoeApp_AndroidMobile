package model

import android.content.Context
import com.google.firebase.Timestamp

import java.io.Serializable
data class ProductImage(
    val imageUrl: String = "",
    val isPrimary: Boolean = false
):Serializable

data class Product(
    val id: String = "",                    // Firestore document ID
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stockQuantity: Int = 0,
    val createdAt: Timestamp? = null,
    val categoryId: String = "",
    val brand: String? = null,
    val material: String? = null,
    val color: String? = null,
    val sizeChartUrl: String? = null,
    val images: List<ProductImage> = emptyList()
):Serializable {
    fun getPrimaryImageResId(context: Context): Int {
        val primaryImage = images.firstOrNull { it.isPrimary }?.imageUrl
            ?: images.firstOrNull()?.imageUrl
            ?: return 0

        return context.resources.getIdentifier(primaryImage, "drawable", context.packageName)
    }
}
