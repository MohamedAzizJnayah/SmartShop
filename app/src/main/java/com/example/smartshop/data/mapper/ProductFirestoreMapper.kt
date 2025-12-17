package com.example.smartshop.data.mapper

import com.example.smartshop.data.local.entity.ProductEntity
import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity

fun ProductFirestoreEntity.toRoom() = ProductEntity(
    id = id,
    name = name,
    quantity = quantity,
    price = price,
    updatedAt = updatedAt
)