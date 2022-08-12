package com.frolo.muse.di.impl.local

import android.content.Context
import com.frolo.muse.repository.FirebasePreferences
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable


class FirebasePreferencesImpl(context: Context) : FirebasePreferences {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun getMessagingToken(): Flowable<String> {
        return RxPreference.ofString(prefs, KEY_MESSAGING_TOKEN).get("")
    }

    override fun setMessagingToken(token: String): Completable {
        return RxPreference.ofString(prefs, KEY_MESSAGING_TOKEN).set(token)
    }

    companion object {
        private const val PREFS_NAME = "com.frolo.muse.firebase_preferences"

        private const val KEY_MESSAGING_TOKEN = "messaging_token"
    }

}