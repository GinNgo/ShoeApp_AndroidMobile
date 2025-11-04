package model.Order

import com.google.firebase.Timestamp
import model.Product

data class Order (
    val id: String = "",
    val userId: String,
    val createdAt: Timestamp? = null,
    val updateAt: Timestamp? = null,
    val product: Product, // id, name, price, imgs
    val quantity: Int,
    val status: OrderStatus = OrderStatus.IN_DELIVERY,
    val totalPrice: Double
)

enum class OrderStatus {
    IN_DELIVERY,
    COMPLETE,
    CANCEL
}
