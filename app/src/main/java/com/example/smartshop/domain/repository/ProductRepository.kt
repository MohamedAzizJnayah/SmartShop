package com.example.smartshop.domain.repository

import com.example.smartshop.domain.model.Product
import com.example.smartshop.domain.model.StockStats
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>
    fun observeStats(): Flow<StockStats>

    suspend fun upsert(product: Product)   // add OR update
    suspend fun delete(productId: String)

    fun startRemoteSync()
    fun stopRemoteSync()
}