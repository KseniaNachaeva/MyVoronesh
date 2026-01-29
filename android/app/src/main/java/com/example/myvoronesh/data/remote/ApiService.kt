package com.example.myvoronesh.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    // ===== Авторизация =====

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // ===== Профиль =====

    @GET("profile")
    suspend fun getProfile(): UserDto

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserDto

    // ===== Аватарка =====

    @Multipart
    @POST("profile/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): ApiResponse<AvatarUploadResponse>

    @DELETE("profile/avatar")
    suspend fun deleteAvatar(): ApiResponse<Any>

    // ===== Квесты =====

    @GET("quests")
    suspend fun getQuests(): List<QuestDto>

    @GET("quests/selected")
    suspend fun getSelectedQuests(): List<QuestDto>

    @POST("quests/{questId}/toggle")
    suspend fun toggleQuest(@Path("questId") questId: String): ApiResponse<Unit>

    @PATCH("quests/{questId}/enabled")
    suspend fun setQuestEnabled(
        @Path("questId") questId: String,
        @Body request: SetQuestEnabledRequest
    ): ApiResponse<Unit>

    // ===== Точки =====

    @GET("points")
    suspend fun getPoints(@Query("questId") questId: String? = null): List<PointDto>

    @GET("points/visible")
    suspend fun getVisiblePoints(): List<PointDto>

    @POST("points/{pointId}/visited/toggle")
    suspend fun togglePointVisited(@Path("pointId") pointId: String): ApiResponse<Unit>

    // ===== Фотографии =====

    @Multipart
    @POST("photos")
    suspend fun uploadPhoto(
        @Part("point_id") pointId: RequestBody,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part
    ): ApiResponse<PhotoUploadResponse>

    @GET("photos/point/{pointId}")
    suspend fun getPointPhotos(@Path("pointId") pointId: String): ApiResponse<List<PhotoDto>>

    @GET("photos/my")
    suspend fun getMyPhotos(): ApiResponse<MyPhotosData>

    @DELETE("photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: String): ApiResponse<Any>

    // ===== Статистика =====

    @GET("stats")
    suspend fun getStats(): ApiResponse<StatsDto>
}

