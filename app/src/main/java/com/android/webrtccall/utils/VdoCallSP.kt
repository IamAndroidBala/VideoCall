package com.android.webrtccall.utils

import android.content.Context
import android.content.SharedPreferences

class VdoCallSP (context: Context) {

    var pref_name: String = "TimeSheet"
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(pref_name, 0)

    fun ClearSP() {
        sharedPreferences.edit().clear().apply()
    }

    fun Remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }


    /**
     * Boolean Values
     */

    var loggedIn: Boolean
        get() = sharedPreferences.getBoolean(AppConstants.LOGGED_IN, false)
        set(value) = sharedPreferences.edit().putBoolean(AppConstants.LOGGED_IN, value).apply()


    /**
     * String
     */

    var email:  String?
        get() = sharedPreferences.getString(AppConstants.EMAIL, "")
        set(value) = sharedPreferences.edit().putString(AppConstants.EMAIL, value).apply()

    var userId: String?
        get() = sharedPreferences.getString(AppConstants.USER_ID, "")
        set(value) = sharedPreferences.edit().putString(AppConstants.USER_ID, value).apply()

    var name: String?
        get() = sharedPreferences.getString(AppConstants.USERNAME, "")
        set(value) = sharedPreferences.edit().putString(AppConstants.USERNAME, value).apply()

    var fcm:    String?
        get() = sharedPreferences.getString(AppConstants.FCM_ID, "")
        set(value) = sharedPreferences.edit().putString(AppConstants.FCM_ID, value).apply()


    /**
     * Integer values
     */

    var userType: Int
        get() = sharedPreferences.getInt(AppConstants.USER_TYPE, 0)
        set(value) = sharedPreferences.edit().putInt(AppConstants.USER_TYPE, value).apply()
}