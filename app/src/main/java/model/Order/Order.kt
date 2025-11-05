package model

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.Date

// ⭐️ Dùng lại file này cho cả 'OrderActivity' của bạn
enum class OrderStatus {
    PROCESSING,  // Đang xử lý
    IN_DELIVERY, // Đang giao
    COMPLETE,    // Hoàn thành
    CANCELLED    // Đã hủy
}

/**
 * Dùng để lưu 1 item bên trong đơn hàng
 * (Nó là bản sao của CartItem)
 */
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val selectedColor: String = "",
    val selectedSize: String = "",
    var quantity: Int = 1,
    var unitPrice: Double = 0.0,
    var isReviewed: Boolean = false,
    val createdAt: Timestamp = Timestamp(Date(1990,1,1))
) : Serializable {
    fun getTotalPrice(): Double = unitPrice * quantity
}

/**
 * Model cho 1 đơn hàng (chứa nhiều OrderItem)
 */
data class Order(
    var id: String = "",
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val status: OrderStatus = OrderStatus.PROCESSING,

    val items: List<OrderItem> = emptyList(), // 👈 Danh sách các sản phẩm

    val totalAmount: Double = 0.0, // 👈 Tổng giá trị đơn hàng

    // (Bạn có thể thêm địa chỉ giao hàng, v.v... vào đây)
    val shippingAddress: Address? = null, // 👈 1. Lưu bản sao địa chỉ
    val discountCode: String? = null,       // 👈 2. Lưu mã voucher đã dùng
    val discountAmount: Double = 0.0      // 👈 3. Lưu tổng tiền đã giảm (SP + Voucher)

) : Serializable