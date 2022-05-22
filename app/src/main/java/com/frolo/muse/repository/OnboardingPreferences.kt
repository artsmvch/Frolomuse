package com.frolo.muse.repository

import io.reactivex.Completable
import io.reactivex.Flowable


interface OnboardingPreferences {
    fun shouldShowOnboarding(): Flowable<Boolean>
    fun markOnboardingDone(): Completable
    fun markOnboardingSkipped(): Completable
}