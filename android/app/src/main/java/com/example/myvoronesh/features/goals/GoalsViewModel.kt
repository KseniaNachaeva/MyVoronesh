package com.example.myvoronesh.features.goals

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myvoronesh.data.remote.StatsDto
import com.example.myvoronesh.features.goals.data.GoalsRepository
import com.example.myvoronesh.features.goals.models.Goal
import com.example.myvoronesh.features.goals.models.GoalStatistic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.myvoronesh.features.goals.mappers.toGoal


data class GoalsUiState(
    val completedQuests: Int = 3,
    val totalQuests: Int = 6,
    val completedPoints: Int = 27,
    val totalPoints: Int = 45,
    val photosTaken: Int = 15,
    val questsProgress: List<Goal> = emptyList(),
    val statistics: List<GoalStatistic> = emptyList(),
    val overallProgress: Int = 0
)

class GoalsViewModel : ViewModel() {

    private val repository = GoalsRepository()

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            repository.getStats().fold(
                onSuccess = { stats: StatsDto ->

                    val goals = stats.questsProgress
                        .orEmpty()
                        .map { it.toGoal() }
                        .sortedByDescending { it.completed.toFloat() / it.total }
                        .take(4)

                        .mapIndexed { index, goal ->
                            goal.copy(color = PastelGoalColors[index])
                        }


                    val statistics = listOf(
                        GoalStatistic(
                            label = "Квесты",
                            value = "${stats.completedQuests}/${stats.totalQuests}",
                            color = Color(0xFFFFD54F)
                        ),
                        GoalStatistic(
                            label = "Точки",
                            value = "${stats.visitedPoints}/${stats.totalPoints}",
                            color = Color(0xFF4CAF50)
                        ),
                        GoalStatistic(
                            label = "Фото",
                            value = stats.photosCount.toString(),
                            color = Color(0xFF2196F3)
                        )
                    )

                    val overallProgress =
                        if (stats.totalPoints > 0)
                            stats.visitedPoints * 100 / stats.totalPoints
                        else 0

                    _uiState.value = GoalsUiState(
                        completedQuests = stats.completedQuests,
                        totalQuests = stats.totalQuests,
                        completedPoints = stats.visitedPoints,
                        totalPoints = stats.totalPoints,
                        photosTaken = stats.photosCount,
                        questsProgress = goals,
                        statistics = statistics,
                        overallProgress = overallProgress
                    )

                },
                onFailure = { error: Throwable ->
                }
            )
        }
    }

}
