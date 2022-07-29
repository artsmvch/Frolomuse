package com.frolo.muse.di.impl.local

import android.content.Context
import android.content.SharedPreferences
import com.frolo.muse.repository.AppLaunchInfoProvider
import com.frolo.muse.repository.OnboardingPreferences
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable


class OnboardingPreferencesImpl(
    private val context: Context,
    private val launchInfoProvider: AppLaunchInfoProvider
) : OnboardingPreferences {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

//    override fun shouldShowOnboarding(): Flowable<Boolean> {
//        return RxPreference.ofBoolean(prefs, KEY_ONBOARDING_PASSED)
//            .get(false)
//            .map { passed ->
//                !passed && launchInfoProvider.isFirstLaunch
//            }
//    }
//
//    override fun markOnboardingDone(): Completable {
//        return RxPreference.ofBoolean(prefs, KEY_ONBOARDING_PASSED).set(true)
//    }
//
//    override fun markOnboardingSkipped(): Completable {
//        return RxPreference.ofBoolean(prefs, KEY_ONBOARDING_PASSED).set(true)
//    }

    private companion object {
        private const val PREFS_NAME = "com.frolo.muse.di.impl.local.ONBOARDING"

        private const val KEY_ONBOARDING_PASSED = "onboarding_passed"
    }
}