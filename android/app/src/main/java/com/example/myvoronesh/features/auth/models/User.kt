package com.example.myvoronesh.features.auth.models

data class User(
    val id: String,
    val login: String,
    val email: String? = null
)