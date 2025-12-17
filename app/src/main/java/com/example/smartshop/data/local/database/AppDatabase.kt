package com.example.smartshop.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.smartshop.data.local.dao.ProductDao
import com.example.smartshop.data.local.entity.ProductEntity
import com.example.smartshop.data.local.entity.User

@Database(entities = [ProductEntity::class,User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}