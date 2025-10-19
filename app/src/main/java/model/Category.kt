package model

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.Date

data class Category(
    val id: String = "",
    val name: String = "",
    val createdAt:  Date? = null,
    val description: String? = null
) : Serializable