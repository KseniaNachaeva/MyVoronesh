package com.example.myvoronesh.features.goals.models

import androidx.compose.ui.graphics.Color

data class Goal(
    val id: String,
    val name: String,
    val completed: Int,
    val total: Int,
    val color: Color
)

data class GoalStatistic(
    val label: String,
    val value: String,
    val color: Color
)