package model

import com.google.firebase.Timestamp

data class Category(
    val name: String = "",
    val createdAt: Timestamp? = null,
    val description: String? = null
)