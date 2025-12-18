package com.example.smartshop.data.mapper


import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity
import com.example.smartshop.domain.model.Product

fun ProductFirestoreEntity.toRoom() = Product(
    id = id,
    name = name,
    quantity = quantity,
    price = price,
    updatedAt = updatedAt
)
fun Product.toFirestoreEntity() = ProductFirestoreEntity(
    id = id,
    name = name,
    quantity = quantity,
    price = price,
    updatedAt = updatedAt
)
