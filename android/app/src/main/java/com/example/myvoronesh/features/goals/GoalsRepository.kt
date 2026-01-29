package com.example.myvoronesh.features.goals.data

import com.example.myvoronesh.data.remote.ApiClient
import com.example.myvoronesh.data.remote.StatsDto

class GoalsRepository {

    suspend fun getStats(): Result<StatsDto> {
        return try {
            val response = ApiClient.apiService.getStats()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Ошибка загрузки статистики"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
