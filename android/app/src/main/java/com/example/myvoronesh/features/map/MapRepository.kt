package com.example.myvoronesh.features.map

import android.content.Context
import com.example.myvoronesh.R
import com.example.myvoronesh.data.remote.ApiClient

import com.example.myvoronesh.features.map.models.QuestPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object MapRepository {

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            ApiClient.init(context)
            isInitialized = true
        }
    }

    fun getQuestPoints(): Flow<List<QuestPoint>> = flow {
        try {
            // getPoints() возвращает List<PointDto>, а не ApiResponse!
            val pointsDto = ApiClient.apiService.getPoints()
            val points = pointsDto.map { dto ->
                QuestPoint(
                    id = dto.id,
                    questId = dto.questId,
                    title = dto.title,
                    description = dto.description,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    address = dto.address,
                    imageUrl = dto.imageFullUrl,
                    visited = dto.visited == 1
                )
            }
            emit(points)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(getLocalPoints())
        }
    }

    suspend fun getVisiblePoints(): List<QuestPoint> {
        return try {
            // getVisiblePoints() тоже возвращает List<PointDto>
            val pointsDto = ApiClient.apiService.getVisiblePoints()
            pointsDto.map { dto ->
                QuestPoint(
                    id = dto.id,
                    questId = dto.questId,
                    title = dto.title,
                    description = dto.description,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    address = dto.address,
                    imageUrl = dto.imageFullUrl,
                    visited = dto.visited == 1
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun togglePointVisited(pointId: String, visited: Boolean): Boolean {
        return try {
            // togglePointVisited возвращает ApiResponse<Unit>
            val response = ApiClient.apiService.togglePointVisited(pointId)
            // Поскольку это ApiResponse — можно использовать .success
            // Но только если ApiResponse определён правильно!
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getLocalPoints(): List<QuestPoint> {
        return listOf(
            QuestPoint("1", "literature", "Памятник Пушкину", "Памятник великому русскому поэту", 51.673362, 39.208631),
            QuestPoint("2", "literature", "Библиотека им. Никитина", "Главная библиотека города", 51.671782, 39.210547),
            QuestPoint("3", "architecture", "Успенский храм", "Адмиралтейская площадь", 51.673858, 39.211716),
            QuestPoint("4", "architecture", "Каменный мост", "Исторический мост", 51.672585, 39.210892),
            QuestPoint("5", "parks", "Кольцовский сквер", "Центральный сквер города", 51.665946, 39.202068),
            QuestPoint("6", "parks", "Петровский сквер", "Памятник Петру I", 51.672200, 39.213400),
            QuestPoint("7", "street_art", "Граффити на Плехановской", "Стрит-арт", 51.668123, 39.199456),
            QuestPoint("8", "music", "Театр оперы и балета", "Главный театр города", 51.659756, 39.200711),
            QuestPoint("9", "music", "Филармония", "Концертный зал", 51.667834, 39.205623)
        )
    }
}