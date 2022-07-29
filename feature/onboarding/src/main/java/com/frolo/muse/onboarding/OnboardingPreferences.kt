package com.frolo.muse.onboarding

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


internal object OnboardingPreferences {
    private const val PREFS_NAME = "com.frolo.muse.onboarding.ONBOARDING"

    private const val KEY_ONBOARDING_PASSED = "onboarding_passed"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isOnboardingPassed(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_ONBOARDING_PASSED, false)
    }

    fun setOnboardingPassed(context: Context) {
        return getPreferences(context).edit {
            putBoolean(KEY_ONBOARDING_PASSED, true)
        }
    }
}