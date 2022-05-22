package com.frolo.muse.ui.main.onboarding


sealed class OnboardingResult {
    abstract val isSkipped: Boolean

    object Passed : OnboardingResult() {
        override val isSkipped: Boolean = false
    }

    object Skipped : OnboardingResult() {
        override val isSkipped: Boolean = true
    }
}