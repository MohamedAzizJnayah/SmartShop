package com.example.smartshop.data.remote.firebase
import com.example.smartshop.data.local.entity.User
import com.example.smartshop.data.mapper.toDomainUser
import com.google.firebase.auth.FirebaseAuth

import kotlinx.coroutines.tasks.await

class AuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Login d'un utilisateur
    suspend fun login(email: String, password: String): User? {
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
    suspend fun register(email: String, password: String): User? {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            auth.currentUser?.toDomainUser()
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

}