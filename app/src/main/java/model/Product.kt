package com.example.shoesapp.model

import java.io.Serializable

data class Product(
    val name: String,
    val price: String,
    val rating: String,
    val sold: String,
    val imageResId: Int,
    val description: String =""
): Serializable
