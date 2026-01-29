package com.example.myvoronesh.data.remote

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "myvoronesh_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_LOGIN = "user_login"
        private const val KEY_USER_EMAIL = "user_email"

        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userLogin: String?
        get() = prefs.getString(KEY_USER_LOGIN, null)
        set(value) = prefs.edit().putString(KEY_USER_LOGIN, value).apply()

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrEmpty()

    fun saveAuthData(token: String, userId: String, userName: String?, userLogin: String?, email: String? = null) {
        this.token = token
        this.userId = userId
        this.userName = userName
        this.userLogin = userLogin
        this.userEmail = email
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}