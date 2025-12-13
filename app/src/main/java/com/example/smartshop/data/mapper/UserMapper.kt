package com.example.smartshop.data.mapper

import com.example.smartshop.data.local.entity.User
import com.google.firebase.auth.FirebaseUser


    fun FirebaseUser.toDomainUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName
        )
    }
