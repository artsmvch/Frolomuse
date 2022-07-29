package com.frolo.muse.onboarding

import android.content.Context
import android.content.Intent


object Onboarding {
    fun isOnboardingPassed(context: Context): Boolean {
        return OnboardingPreferences.isOnboardingPassed(context)
    }

    fun createOnboardingIntent(context: Context): Intent {
        return OnboardingActivity.newIntent(context)
    }
}