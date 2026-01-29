package com.example.myvoronesh.features.profile.models

data class Profile(
    val name: String = "",
    val birthDate: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val avatarFullUrl: String? = null
)