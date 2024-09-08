package com.shadspace.kahani

import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager {

    private const val PREF_NAME = "UserPrefs"
    private const val IS_LOGIN = "IsLogin"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_email"


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

    // Save user email
    fun setUserEmail(context: Context, email: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    // Retrieve user email
    fun getUserEmail(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }


    // Clear all preferences (e.g., on logout)
    fun clearPreferences(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
