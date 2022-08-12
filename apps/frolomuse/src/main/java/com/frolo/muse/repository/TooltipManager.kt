package com.frolo.muse.repository

import com.frolo.muse.model.TooltipId
import io.reactivex.Completable
import io.reactivex.Single


interface TooltipManager {
    /**
     * Returns true, if [tooltipId] was not shown before and can be shown now.
     */
    fun canShowTooltip(tooltipId: TooltipId): Single<Boolean>

    /**
     * Marks [tooltipId] as shown (the tooltip is considered processed).
     */
    fun markTooltipShown(tooltipId: TooltipId): Completable

    /**
     * Returns true, if [tooltipId] was not shown before and can be shown now. In any case,
     * the tooltip is marked as shown and is considered processed. In fact, this method is
     * a combination of [canShowTooltip] and [markTooltipShown].
     */
    fun processTooltip(tooltipId: TooltipId): Single<Boolean>
}