package com.example.smartshop.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val updatedAt: Long = System.currentTimeMillis()
)