package com.frolo.muse.di.impl.local

import android.content.Context
import android.content.SharedPreferences
import com.frolo.muse.repository.PlaylistTransferPreferences
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Single


class PlaylistTransferPreferencesImpl(context: Context): PlaylistTransferPreferences {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun requestTransfer(): Single<Boolean> {
        return RxPreference.ofBoolean(prefs, KEY_TRANSFER_COMPLETED)
            .get(false)
            .first(false)
            .map { completed -> !completed }
    }

    override fun completeTransfer(): Completable {
        return RxPreference.ofBoolean(prefs, KEY_TRANSFER_COMPLETED).set(true)
    }

    companion object {
        private const val PREFS_NAME = "com.frolo.muse.PlaylistTransfer"

        private const val KEY_TRANSFER_COMPLETED = "transfer_completed"
    }

}