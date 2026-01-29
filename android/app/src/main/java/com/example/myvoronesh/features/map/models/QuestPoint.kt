package com.example.myvoronesh.features.map.models

import com.example.myvoronesh.R

data class QuestPoint(
    val id: String,
    val questId: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val imageRes: Int = R.drawable.dost,
    val imageUrl: String? = null,
    val iconRes: Int = R.drawable.pin,
    val visited: Boolean = false
)