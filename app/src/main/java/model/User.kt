package model

import com.google.firebase.Timestamp

data class                                                                                                                      User(
    val id: String? = "",
    val username: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val createdAt: Timestamp? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val shippingAddress: String? = null,
    val billingAddress: String? = null,
    val phoneNumber: String? = null,
    val role: Int = 0,
    val gender: String? = null,
    val date: Timestamp? = null,
    val phone: String  = ""
)