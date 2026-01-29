package com.example.myvoronesh.features.album.models

data class Photo(
    val id: String,
    val imageRes: Int = 0,
    val imageUrl: String? = null,
    val description: String
)

data class LocationPhotos(
    val pointId: String,
    val locationName: String,
    val questName: String,
    val date: String,
    val photos: List<Photo>
)