package com.example.smartshop.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.smartshop.data.local.dao.ProductDao
import com.example.smartshop.domain.model.Product
import com.example.smartshop.domain.model.User



@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
