package com.example.myvoronesh.core.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_DARK_THEME = "dark_theme"

    var isDarkTheme by mutableStateOf(false)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false)
    }

    fun toggle(context: Context) {
        isDarkTheme = !isDarkTheme
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_THEME, isDarkTheme)
            .apply()
    }
}
