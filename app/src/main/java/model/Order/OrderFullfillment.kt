package model

import com.google.firebase.Timestamp

data class OrderFullfillment(
    val id: String,
    val orderId: String,
    val time: Timestamp,
    val address: String,
    val status: FullFillmentStatus
)

enum class FullFillmentStatus {
    VERIFIED_PAYMENT,
    PACKING,
    SHIPPED,
    CUSTOMS_PORT,
    IN_TRANSIT
}
