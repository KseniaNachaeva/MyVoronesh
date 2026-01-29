package com.example.myvoronesh.features.quests

import android.content.Context
import com.example.myvoronesh.R
import com.example.myvoronesh.data.remote.ApiClient
import com.example.myvoronesh.data.remote.SetQuestEnabledRequest

import com.example.myvoronesh.features.quests.models.Quest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object QuestsRepository {

    private val _allQuests = MutableStateFlow<List<Quest>>(emptyList())
    val allQuests: StateFlow<List<Quest>> = _allQuests

    private val _selectedQuestIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedQuestIds: StateFlow<Set<String>> = _selectedQuestIds

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            ApiClient.init(context)
            isInitialized = true
        }
    }

    suspend fun loadQuests() {
        try {
            // getQuests() возвращает List<QuestDto> напрямую
            val questDtos = ApiClient.apiService.getQuests()

            // Преобразуем в Quest
            _allQuests.value = questDtos.map { dto ->
                Quest(
                    id = dto.id,
                    title = dto.title,
                    isEnabled = dto.isEnabled == 1,
                    imageRes = getImageRes(dto.id)
                )
            }

            // Выбираем отмеченные квесты (isSelected == 1)
            _selectedQuestIds.value = questDtos
                .filter { it.isSelected == 1 }
                .map { it.id }
                .toSet()

        } catch (e: Exception) {
            e.printStackTrace()
            loadLocalQuests()
        }
    }

    private fun loadLocalQuests() {
        _allQuests.value = listOf(
            Quest("literature", "Литература", false, R.drawable.vor),
            Quest("architecture", "Архитектура", false, R.drawable.vor2),
            Quest("parks", "Парки и скверы", false, R.drawable.vor3),
            Quest("street_art", "Уличное искусство", false, R.drawable.vor4),
            Quest("music", "Музыкальный квест", false, R.drawable.vor5),
            Quest("literary", "Литературный квест", false, R.drawable.vor6)
        )
    }

    private fun getImageRes(questId: String): Int {
        return when (questId) {
            "literature" -> R.drawable.vor
            "architecture" -> R.drawable.vor2
            "parks" -> R.drawable.vor3
            "street_art" -> R.drawable.vor4
            "music" -> R.drawable.vor5
            "literary" -> R.drawable.vor6
            else -> R.drawable.vor
        }
    }

    suspend fun toggleQuestSelection(questId: String) {
        try {
            // Передаём только ID через URL, без ToggleQuestRequest
            val response = ApiClient.apiService.toggleQuest(questId)

            // Поскольку toggleQuest возвращает ApiResponse<Unit>,
            // предполагаем, что response — это ApiResponse с success
            if (response.success) {
                // Но!ApiResponse<Unit> не содержит данных о is_selected!
                // → Значит, вы не можете получить `is_selected` из ответа.

                // ❗ Здесь логическая проблема: если бэкенд не возвращает состояние,
                // вы не знаете, включён квест или выключен.

                // Обычно в таких случаях делают:
                // - либо возвращают новое состояние в ответе,
                // - либо просто перезагружают список квестов после toggle.

                // Решение: перезагрузите квесты
                loadQuests()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Локальное переключение
            val current = _selectedQuestIds.value
            _selectedQuestIds.value = if (questId in current) {
                current - questId
            } else {
                current + questId
            }
        }
    }

    fun isQuestSelected(questId: String): Boolean {
        return _selectedQuestIds.value.contains(questId)
    }

    suspend fun setQuestEnabled(questId: String, enabled: Boolean) {
        try {
            ApiClient.apiService.setQuestEnabled(
                questId = questId,
                request = SetQuestEnabledRequest(enabled = enabled)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _allQuests.value = _allQuests.value.map { quest ->
            if (quest.id == questId) quest.copy(isEnabled = enabled) else quest
        }
    }

    fun getQuestsForDialog(): List<Quest> {
        return _allQuests.value.filter { quest ->
            _selectedQuestIds.value.contains(quest.id)
        }
    }

    fun getActiveQuestIds(): Set<String> {
        return _allQuests.value
            .filter { it.isEnabled }
            .map { it.id }
            .toSet()
    }
}