package com.example.smartshop.data.remote.firebase.entity

data class ProductFirestoreEntity(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val updatedAt: Long = 0L
)