package com.example.smartshop.data.repositoryImplementation

import com.example.smartshop.data.local.entity.User
import com.example.smartshop.data.remote.firebase.AuthService
import com.example.smartshop.domain.repositoryInterfaces.AuthRepository

class AuthRepositoryImpl(
    private val service: AuthService = AuthService()
) : AuthRepository {

    override suspend fun login(email: String, password: String): User? {
        return service.login(email, password)
    }

    override suspend fun register(email: String, password: String): User? {
        return service.register(email, password)
    }

    override fun logout() {
        service.logout()
    }

}
