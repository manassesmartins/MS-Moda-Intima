package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("atelier_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USING_SUPABASE = "using_supabase"
    }

    fun saveSession(userId: String, email: String, authToken: String? = null, usingSupabase: Boolean = false) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_AUTH_TOKEN, authToken)
            putBoolean(KEY_USING_SUPABASE, usingSupabase)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    val isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    val userId: String?
        get() = prefs.getString(KEY_USER_ID, null)

    val userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)

    val authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)

    val isUsingSupabase: Boolean
        get() = prefs.getBoolean(KEY_USING_SUPABASE, false)
}
