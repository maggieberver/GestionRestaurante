package com.example.examen2.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("restaurant_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLE = "role"
    }

    fun saveUserSession(user: User) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_ROLE, user.role)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun getFullName(): String = prefs.getString(KEY_FULL_NAME, "") ?: ""

    fun getRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun logout() {
        prefs.edit().clear().apply()
    }
}
