package model

import java.io.Serializable
import java.util.Date

// Enum để định nghĩa loại giảm giá
enum class DiscountType {
    PERCENTAGE, // Giảm theo %
    FIXED_AMOUNT  // Giảm số tiền cố định
}

data class Voucher(
    val id: String = "",
    val code: String = "",          // Mã voucher (vd: "SALE100K")
    val description: String = "",   // Mô tả (vd: "Giảm 100k cho đơn trên 500k")

    val discountType: DiscountType = DiscountType.FIXED_AMOUNT,
    val discountValue: Double = 0.0, // Giá trị giảm (vd: 10.0 cho 10% hoặc 100000.0 cho 100k)

    val minOrderValue: Double = 0.0, // Giá trị đơn hàng tối thiểu
    val maxDiscountAmount: Double? = null, // Giảm tối đa (quan trọng cho loại PERCENTAGE)

    val expirationDate: Date? = null, // Ngày hết hạn
    val usageLimit: Int = 0,        // Số lần sử dụng tối đa
    val usageCount: Int = 0,        // Số lần đã sử dụng

    val isActive: Boolean = true,     // Đang hoạt động hay không
    val createdAt: Date? = null
) : Serializable