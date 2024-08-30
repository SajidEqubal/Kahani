package com.shadspace.kahani

import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager {

    private const val PREF_NAME = "UserPrefs"
    private const val IS_LOGIN = "IsLogin"

    // Initialize SharedPreferences
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save login status
    fun setLogin(context: Context, isLogin: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(IS_LOGIN, isLogin)
        editor.apply()
    }

    // Get login status
    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(IS_LOGIN, false)
    }

    // Clear all preferences (e.g., on logout)
    fun clearPreferences(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
