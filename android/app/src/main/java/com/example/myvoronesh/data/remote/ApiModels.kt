package com.example.myvoronesh.data.remote


// ===== Базовый ответ API =====
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

// ===== Авторизация =====
data class LoginRequest(
    val login: String,
    val password: String
)

data class RegisterRequest(
    val login: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val userId: String? = null,
    val user: UserDto? = null,
    val message: String? = null
)

// ===== Пользователь =====

data class UserDto(
    val id: String,
    val login: String,
    val name: String,
    val email: String? = null,
    val birthDate: String? = null,
    val avatarUrl: String? = null,
    val avatarFullUrl: String? = null
)

data class UpdateProfileRequest(
    val name: String,
    val email: String?,
    val birthDate: String?
)

// ===== Квесты =====
data class QuestDto(
    val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val color: String?,
    val orderIndex: Int,
    val isSelected: Int = 0,
    val isEnabled: Int = 0,
    val totalPoints: Int = 0,
    val completedPoints: Int = 0
)

data class SetQuestEnabledRequest(
    val enabled: Boolean
)

// ===== Точки =====
data class PointDto(
    val id: String,
    val questId: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val imageUrl: String?,
    val imageFullUrl: String?,
    val questTitle: String? = null,
    val questColor: String? = null,
    val visited: Int = 0
)

// ===== Статистика =====
data class StatsDto(
    val completedQuests: Int,
    val totalQuests: Int,
    val visitedPoints: Int,
    val totalPoints: Int,
    val photosCount: Int,
    val questsProgress: List<QuestProgressDto>?
)

data class QuestProgressDto(
    val id: String,
    val title: String,
    val color: String?,
    val total: Int,
    val completed: Int
)

// ===== Фотографии =====
data class PhotoUploadResponse(
    val id: String,
    val photoUrl: String,
    val fullUrl: String,
    val description: String?,
    val pointId: String
)

data class PhotoDto(
    val id: String,
    val photoUrl: String,
    val fullUrl: String,
    val description: String?,
    val createdAt: String?
)

data class LocationPhotosDto(
    val pointId: String,
    val locationName: String,
    val questName: String,
    val date: String,
    val photos: List<PhotoDto>
)

data class MyPhotosData(
    val data: List<LocationPhotosDto>,
    val total: Int
)

// ===== Аватарка =====
data class AvatarUploadResponse(
    val avatarUrl: String,
    val fullUrl: String
)