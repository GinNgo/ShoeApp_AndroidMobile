package model

import java.io.Serializable

data class CartItem(
    var id: String = "",            // ID của CartItem (duy nhất)
    val userId: String = "",        // ID của người dùng

    // --- Thông tin sản phẩm (sao chép) ---
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",    // Tên ảnh drawable hoặc URL

    // --- Biến thể (Variant) đã chọn ---
    val selectedColor: String = "", // Tên màu, vd: "Đen"
    val selectedSize: String = "",    // Tên size, vd: "41"

    // --- Giá và Số lượng ---
    var quantity: Int = 1,
    var price: Double = 0.0,        // 👈 Giá GỐC của sản phẩm
    var salePrice: Double? = null   // 👈 Giá SALE (nếu có)

) : Serializable {

    /**
     * Lấy giá hiển thị (ưu tiên giá sale)
     */
    fun getDisplayPrice(): Double {
        return salePrice ?: price
    }

    /**
     * Lấy tổng tiền cho item này (dựa trên giá hiển thị)
     */
    fun getTotalPrice(): Double {
        return getDisplayPrice() * quantity
    }

    /**
     * ⭐️ (MỚI) Tính toán số tiền tiết kiệm được CHỈ TỪ SẢN PHẨM
     */
    fun getProductDiscount(): Double {
        if (salePrice != null && salePrice!! < price) {
            return (price - salePrice!!) * quantity
        }
        return 0.0
    }
}