package utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val SESSION_TIMEOUT = 15 * 60 * 1000 // 15 phút (ms)
    }

    fun saveUserSession(email: String, role: Int) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putInt(KEY_ROLE, role)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getUserSession(): Pair<String, Int>? {
        val email = prefs.getString(KEY_EMAIL, null)
        val role = prefs.getInt(KEY_ROLE, -1)
        val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0)

        if (email != null && role != -1) {
            val now = System.currentTimeMillis()
            if (now - loginTime <= SESSION_TIMEOUT) {
                return Pair(email, role)
            } else {
                clearSession() // Hết hạn 15 phút thì xóa
            }
        }
        return null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}