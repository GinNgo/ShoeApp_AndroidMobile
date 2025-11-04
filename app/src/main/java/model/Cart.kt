package model

import com.google.firebase.Timestamp
import model.OrderStatus

data class Cart (
    val id: String = "",
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val products: Map<String, Int> = emptyMap(),
    val exist: Boolean = true
)

