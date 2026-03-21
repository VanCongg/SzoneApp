package com.app.szone.data.local


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.app.szone.domain.repository.WelcomeRepository

class WelcomeRepositoryImpl(context: Context) : WelcomeRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(ON_BOARDING_PREF_NAME, Context.MODE_PRIVATE)

    override fun saveOnBoardingState(completed: Boolean) {
        prefs.edit {
            putBoolean(ON_BOARDING_KEY, completed)
        }
    }

    override fun getOnBoardingState(): Boolean {
        return prefs.getBoolean(ON_BOARDING_KEY, false)
    }

    companion object {
        private const val ON_BOARDING_PREF_NAME = "ON_BOARDING_PREF_NAME"
        private const val ON_BOARDING_KEY = "ON_BOARDING_KEY"
    }
}
