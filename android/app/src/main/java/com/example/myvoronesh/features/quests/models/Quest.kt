package com.example.myvoronesh.features.quests.models

data class Quest(
    val id: String,
    val title: String,
    val isEnabled: Boolean,
    val imageRes: Int
)