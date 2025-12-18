package com.example.smartshop.data.remote.firebase.authentication

import com.example.smartshop.domain.model.User
import com.example.smartshop.data.mapper.toDomainUser
import com.example.smartshop.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val auth: FirebaseAuth
): AuthRepository {
    // Login d'un utilisateur
    override suspend fun login(email: String, password: String): User? {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            auth.currentUser?.toDomainUser()
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUser(): User? {
        return auth.currentUser?.toDomainUser()
    }

    //Register d'un nouvelle utilisateur
    override suspend fun register(email: String, password: String): User? {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            auth.currentUser?.toDomainUser()
        } catch (e: Exception) {
            null
        }
    }

    override fun logout() {
        auth.signOut()
    }

}