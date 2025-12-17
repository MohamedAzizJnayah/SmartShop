package com.example.smartshop.domain.model

data class Product(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val updatedAt: Long = System.currentTimeMillis()
)