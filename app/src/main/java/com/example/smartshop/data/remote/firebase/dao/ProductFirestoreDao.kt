package com.example.smartshop.data.remote.firebase.dao

import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity
import com.example.smartshop.data.remote.firebase.sync.RemoteProductChange
import kotlinx.coroutines.flow.Flow

interface ProductFirestoreDao {
    fun observeAll(): Flow<List<ProductFirestoreEntity>>   // temps réel
    suspend fun upsert(product: ProductFirestoreEntity)
    suspend fun delete(id: String)
    fun observeChanges(): Flow<RemoteProductChange>
}