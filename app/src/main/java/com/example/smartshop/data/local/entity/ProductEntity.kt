package com.example.smartshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val updatedAt: Long
)