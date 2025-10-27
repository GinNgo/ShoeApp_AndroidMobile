package model

import android.content.Context
import java.io.Serializable
import java.util.Date

data class ProductImage(
    val imageUrl: String = "",
    val isPrimary: Boolean = false
) : Serializable

data class Product(
    val id: String = "",                    // Firestore document ID
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stockQuantity: Int = 0,
    val createdAt: Date? = null,
    val categoryId: String = "",
    val brand: String? = null,
    val material: String? = null,
    val colors: List<ProductColor> = emptyList(),
    val sizeChartUrl: String? = null,

    // ✅ Ảnh chính hiển thị trong danh sách
    val primaryImageUrl: String = "",

    // ✅ Danh sách ảnh phụ (dùng cho trang chi tiết)
    val images: List<ProductImage> = emptyList()
) : Serializable {

    // 🖼️ Lấy ID ảnh chính trong drawable
    fun getPrimaryImageResId(context: Context): Int {
        // Ưu tiên ảnh chính từ trường primaryImageUrl (nếu có)
        val mainImageName = when {
            primaryImageUrl.isNotBlank() -> primaryImageUrl
            else -> {
                // Nếu không có, lấy ảnh có isPrimary = true trong danh sách
                images.firstOrNull { it.isPrimary }?.imageUrl
                // Nếu vẫn không có, lấy ảnh đầu tiên trong list
                    ?: images.firstOrNull()?.imageUrl
            }
        } ?: return 0

        // Trả về resource ID (0 nếu không tìm thấy)
        return context.resources.getIdentifier(mainImageName, "drawable", context.packageName)
    }

    // 🖼️ Lấy danh sách resource ID của ảnh phụ
    fun getAllImageResIds(context: Context): List<Int> {
        return images.mapNotNull { img ->
            context.resources.getIdentifier(img.imageUrl, "drawable", context.packageName)
                .takeIf { it != 0 }
        }
    }
    data class ProductColor(
        val hexCode: String = "",   // Ví dụ "#FF0000"
        val name: String = "",      // Tên hiển thị: "Đỏ"
        var isOutOfStock: Boolean = false // Đã hết hàng chưa
    ) : Serializable
}
