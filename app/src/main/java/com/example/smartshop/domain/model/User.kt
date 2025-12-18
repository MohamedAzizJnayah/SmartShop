package com.example.smartshop.domain.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String? = null
)