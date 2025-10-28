package model

import android.content.Context
import java.io.Serializable
import java.util.Date

data class ProductImage(
    val imageUrl: String = "",
    val isPrimary: Boolean = false
) : Serializable

// ⭐️ (MỚI) Data class để lưu trữ biến thể size và tồn kho riêng lẻ
// Bạn có thể đặt class này bên trong Product hoặc bên ngoài
data class ProductSize(
    val size: String = "",        // Ví dụ: "40", "41", "M", "L"
    val stockQuantity: Int = 0  // Tồn kho cho CHỈ size này
) : Serializable

data class Product(
    val id: String = "",                    // Firestore document ID
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,

    // ⭐️ (XÓA) Bỏ tồn kho tổng. Tồn kho sẽ được quản lý bên trong 'ProductSize'
    // val stockQuantity: Int = 0, // <-- XÓA DÒNG NÀY

    val createdAt: Date? = null,
    val categoryIds: List<String> = emptyList(),
    val brand: String? = null,
    val material: String? = null,

    // ⭐️ (CẬP NHẬT) 'colors' bây giờ sẽ chứa danh sách 'sizes' bên trong nó
    val colors: List<ProductColor> = emptyList(),
    val sizeChartUrl: String? = null,

    // ✅ Ảnh chính hiển thị trong danh sách
    val primaryImageUrl: String = "",

    // ✅ Danh sách ảnh phụ (dùng cho trang chi tiết)
    val images: List<ProductImage> = emptyList()
) : Serializable {

    // ⭐️ (HÀM MỚI) Hàm tiện ích để lấy tổng tồn kho của sản phẩm
    fun getTotalStock(): Int {
        // Tính tổng tồn kho của tất cả các size trong tất cả các màu
        return colors.sumOf { color ->
            color.sizes.sumOf { size -> size.stockQuantity }
        }
    }

    // 🖼️ Lấy ID ảnh chính trong drawable
    fun getPrimaryImageResId(context: Context): Int {
        // (Giữ nguyên code)
        val mainImageName = when {
            primaryImageUrl.isNotBlank() -> primaryImageUrl
            else -> {
                images.firstOrNull { it.isPrimary }?.imageUrl
                    ?: images.firstOrNull()?.imageUrl
            }
        } ?: return 0
        return context.resources.getIdentifier(mainImageName, "drawable", context.packageName)
    }

    // 🖼️ Lấy danh sách resource ID của ảnh phụ
    fun getAllImageResIds(context: Context): List<Int> {
        // (Giữ nguyên code)
        return images.mapNotNull { img ->
            context.resources.getIdentifier(img.imageUrl, "drawable", context.packageName)
                .takeIf { it != 0 }
        }
    }

    // ⭐️ (CẬP NHẬT) Data class cho Color
    data class ProductColor(
        val hexCode: String = "",   // Ví dụ "#FF0000"
        val name: String = "",      // Tên hiển thị: "Đỏ"

        // ⭐️ (XÓA) Bỏ isOutOfStock, vì stock được quản lý theo từng size
        // var isOutOfStock: Boolean = false, // <-- XÓA DÒNG NÀY

        // ⭐️ (THÊM) Thêm danh sách các size và tồn kho cho MÀU NÀY
        val sizes: List<ProductSize> = emptyList()

    ) : Serializable
}