package com.example.smartshop.domain.repository

import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity
import com.example.smartshop.domain.model.RemoteProductChange
import com.example.smartshop.domain.model.Product
import com.example.smartshop.domain.model.StockStats
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun observeAll(): Flow<List<Product>>   // temps réel
    suspend fun upsert(product: Product)
    suspend fun delete(id: String)
    fun observeStats(): Flow<StockStats>
    fun observeChanges(): Flow<RemoteProductChange>

}