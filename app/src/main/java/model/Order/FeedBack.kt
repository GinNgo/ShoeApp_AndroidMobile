package model.Order

import com.google.firebase.Timestamp

data class FeedBack (
    val id:String,
    val userId: String,
    val productId: String,
    val orderId : String,
    val rating: Int,
    val review: String,
    val createAt: Timestamp
)