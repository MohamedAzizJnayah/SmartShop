package com.example.smartshop.data.remote.firebase.dao


import com.example.smartshop.data.remote.firebase.entity.ProductFirestoreEntity
import com.example.smartshop.domain.model.Product
import com.example.smartshop.domain.model.RemoteProductChange
import kotlinx.coroutines.flow.Flow

interface ProductFirestoreDao {
    fun observeAll(): Flow<List<Product>>
    fun observeChanges(): Flow<RemoteProductChange>
    suspend fun upsert(entity: ProductFirestoreEntity)
    suspend fun delete(id: String)
}
