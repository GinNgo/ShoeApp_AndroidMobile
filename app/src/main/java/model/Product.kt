package model

import android.content.Context
import android.util.Log
import com.google.j2objc.annotations.Property
import com.google.firebase.firestore.PropertyName
import java.io.Serializable
import java.util.Date

// (ProductImage giữ nguyên)
data class ProductImage(
    val imageUrl: String = "",

    @PropertyName("isPrimary")   // ÉP LƯU THÀNH "isPrimary"
    @get:PropertyName("isPrimary")  // ĐỌC LẠI CŨNG DÙNG "isPrimary"
    val isPrimary: Boolean = false
) : Serializable

// (ProductSize giữ nguyên)
data class ProductSize(
    val size: String = "",
    val stockQuantity: Int = 0
) : Serializable

// ⭐️ (SỬA) DI CHUYỂN ProductColor RA BÊN NGOÀI
data class ProductColor(
    val hexCode: String = "",
    val name: String = "",
    val sizes: List<ProductSize> = emptyList()
) : Serializable

// ⭐️ (SỬA) File Product bây giờ chỉ chứa các trường
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val salePrice: Double? = null, // 👈 Đã thêm
    val createdAt: Date? = null,
    val categoryIds: List<String> = emptyList(), // 👈 Đã sửa
    val brandId: String? = null,
//    val material: String? = null,
    val colors: List<ProductColor> = emptyList(), // 👈 Bây giờ sẽ tham chiếu đúng
//    val sizeChartUrl: String? = null,
    val primaryImageUrl: String = "",
    val images: List<ProductImage> = emptyList(),
    val soldCount: Int = 0
) : Serializable {

    // (Hàm isOnSale giữ nguyên)

    fun getTotalStock(): Int {
        return colors.sumOf { color ->
            color.sizes.sumOf { size -> size.stockQuantity }
        }
    }
    fun isOnSale(): Boolean {
        return salePrice != null && salePrice > 0 && salePrice < price
    }

    // (Hàm getDisplayPrice giữ nguyên)
    fun getDisplayPrice(): Double {
        return if (isOnSale()) salePrice!! else price
    }

    // (Các hàm lấy ảnh giữ nguyên)
    fun getPrimaryImageResId(context: Context): Int {
        val mainImageName = when {
            primaryImageUrl.isNotBlank() -> primaryImageUrl
            else -> {
                images.firstOrNull { it.isPrimary }?.imageUrl
                    ?: images.firstOrNull()?.imageUrl
            }
        } ?: return 0
        return context.resources.getIdentifier(mainImageName, "drawable", context.packageName)
    }

    fun getAllImageResIds(context: Context): List<Int> {
        return images.mapNotNull { img ->
            context.resources.getIdentifier(img.imageUrl, "drawable", context.packageName)
                .takeIf { it != 0 }
        }
    }

}