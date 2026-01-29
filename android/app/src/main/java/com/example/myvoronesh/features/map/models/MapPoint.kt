package com.example.myvoronesh.features.map.models

data class MapPoint(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val questId: String
)