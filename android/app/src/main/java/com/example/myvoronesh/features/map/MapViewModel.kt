package com.example.myvoronesh.features.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvoronesh.features.map.models.QuestPoint
import com.example.myvoronesh.features.quests.QuestsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class BottomNavScreen {
    MAP, PROFILE, GALLERY, QUESTS, GOALS
}

data class MapUiState(
    val selectedScreen: BottomNavScreen = BottomNavScreen.MAP,
    val currentZoom: Float = 12f,
    val questPoints: List<QuestPoint> = emptyList(),
    val visiblePoints: List<QuestPoint> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPoint: QuestPoint? = null
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        MapRepository.init(application)
        QuestsRepository.init(application)
        loadQuestPoints()
        observeActiveQuests()
    }

    private fun loadQuestPoints() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            MapRepository.getQuestPoints().collect { points ->
                _uiState.update {
                    it.copy(
                        questPoints = points,
                        isLoading = false
                    )
                }
                updateVisiblePoints()
            }
        }
    }

    private fun observeActiveQuests() {
        viewModelScope.launch {
            combine(
                QuestsRepository.allQuests,
                QuestsRepository.selectedQuestIds
            ) { quests, selectedIds ->
                quests.filter { quest ->
                    selectedIds.contains(quest.id) && quest.isEnabled
                }
            }.collect {
                updateVisiblePoints()
            }
        }
    }

    private fun updateVisiblePoints() {
        viewModelScope.launch {
            // Пробуем загрузить с сервера
            val serverPoints = MapRepository.getVisiblePoints()

            if (serverPoints.isNotEmpty()) {
                _uiState.update { state -> state.copy(visiblePoints = serverPoints) }
            } else {
                // Локальная фильтрация
                val activeQuestIds = QuestsRepository.getActiveQuestIds()
                _uiState.update { state ->
                    state.copy(
                        visiblePoints = state.questPoints.filter { point ->
                            activeQuestIds.contains(point.questId)
                        }
                    )
                }
            }
        }
    }

    fun toggleVisited(pointId: String, visited: Boolean) {
        viewModelScope.launch {
            MapRepository.togglePointVisited(pointId, visited)

            _uiState.update { state ->
                state.copy(
                    questPoints = state.questPoints.map { point ->
                        if (point.id == pointId) point.copy(visited = visited) else point
                    },
                    visiblePoints = state.visiblePoints.map { point ->
                        if (point.id == pointId) point.copy(visited = visited) else point
                    },
                    selectedPoint = state.selectedPoint?.let { selected ->
                        if (selected.id == pointId) selected.copy(visited = visited) else selected
                    }
                )
            }
        }
    }

    fun selectScreen(screen: BottomNavScreen) {
        _uiState.value = _uiState.value.copy(selectedScreen = screen)
    }

    fun onPointClick(point: QuestPoint) {
        Log.d("MapViewModel", "Point clicked: ${point.title}")

        // 👇 Ищем АКТУАЛЬНУЮ версию точки в обновлённом списке
        val actualPoint = _uiState.value.visiblePoints.find { it.id == point.id }
            ?: _uiState.value.questPoints.find { it.id == point.id }
            ?: point  // fallback если не нашли

        Log.d("MapViewModel", "Actual visited status: ${actualPoint.visited}")

        _uiState.update { it.copy(selectedPoint = actualPoint) }
    }

    fun dismissBottomSheet() {
        Log.d("MapViewModel", "Dismissing bottom sheet")
        _uiState.update { it.copy(selectedPoint = null) }
    }


}