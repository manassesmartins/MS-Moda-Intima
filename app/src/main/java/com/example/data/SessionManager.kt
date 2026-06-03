package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ms_producao_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTH_TOKEN = "auth_token"
        
        // Customization keys
        private const val KEY_APP_NAME = "custom_app_name"
        private const val KEY_COLOR_SCHEME = "custom_color_scheme"
        private const val KEY_DARK_MODE = "custom_dark_mode"
        private const val KEY_FONT_SIZE_SCALE = "custom_font_size_scale"
    }

    var appName: String
        get() = prefs.getString(KEY_APP_NAME, "MS") ?: "MS"
        set(value) = prefs.edit().putString(KEY_APP_NAME, value).apply()

    var colorScheme: String
        get() = prefs.getString(KEY_COLOR_SCHEME, "PINK") ?: "PINK"
        set(value) = prefs.edit().putString(KEY_COLOR_SCHEME, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var fontSizeScale: Float
        get() = prefs.getFloat(KEY_FONT_SIZE_SCALE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE_SCALE, value).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userAvatar: String
        get() = prefs.getString("user_avatar", "") ?: ""
        set(value) = prefs.edit().putString("user_avatar", value).apply()

    fun saveSession(
        userId: String,
        email: String,
        authToken: String? = null,
        name: String? = null,
        avatarUrl: String? = null
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_AUTH_TOKEN, authToken)
            putString("user_name", name ?: "")
            putString("user_avatar", avatarUrl ?: "")
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

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    val authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
}
