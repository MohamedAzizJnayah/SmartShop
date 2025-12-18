package com.example.smartshop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smartshop.domain.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAll(): Flow<List<Product>>

    // Si tu veux vraiment un "upsert" :
    // @Upsert
    // suspend fun upsert(entity: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM products")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity * price), 0) FROM products")
    fun observeTotalValue(): Flow<Double>

    @Query("DELETE FROM products")
    suspend fun clearAll()
}