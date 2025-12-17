package com.example.smartshop.domain.repository

import com.example.smartshop.data.local.entity.User

interface AuthRepository {
    suspend fun login(email: String, password: String): User?
    suspend fun register(email: String, password: String): User?

    fun logout()

}