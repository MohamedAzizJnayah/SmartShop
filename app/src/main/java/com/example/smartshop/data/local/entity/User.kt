package com.example.smartshop.data.local.entity

data class User(
    val uid: String,
    val email: String?,
    val displayName: String? = null
)