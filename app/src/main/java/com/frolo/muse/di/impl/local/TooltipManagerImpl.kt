package com.frolo.muse.di.impl.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.frolo.muse.model.TooltipId
import com.frolo.muse.repository.TooltipManager
import com.frolo.muse.rx.newSingleThreadScheduler
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single


class TooltipManagerImpl(private val context: Context): TooltipManager {

    private val workerScheduler: Scheduler by lazy {
        newSingleThreadScheduler("TooltipManager")
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun canShowTooltip(tooltipId: TooltipId): Single<Boolean> {
        val wasShownDefault = false
        return RxPreference.ofBoolean(prefs, getTooltipWasShownKey(tooltipId))
            .get(wasShownDefault)
            .first(wasShownDefault)
            .map { wasShown -> !wasShown }
    }

    override fun markTooltipShown(tooltipId: TooltipId): Completable {
        return RxPreference.ofBoolean(prefs, getTooltipWasShownKey(tooltipId)).set(true)
    }

    override fun processTooltip(tooltipId: TooltipId): Single<Boolean> {
        val source = Single.fromCallable {
            val key = getTooltipWasShownKey(tooltipId)
            val wasShown = prefs.getBoolean(key, false)
            if (!wasShown) prefs.edit { putBoolean(key, true) }
            return@fromCallable !wasShown
        }
        return source.subscribeOn(workerScheduler)
    }

    companion object {
        private const val PREFS_NAME = "com.frolo.muse.Tooltips"

        private const val KEY_TOOLTIP_WAS_SHOWN = "tooltip_was_shown"

        private fun getTooltipWasShownKey(tooltipId: TooltipId): String {
            return KEY_TOOLTIP_WAS_SHOWN + "_" + tooltipId.key
        }
    }

}