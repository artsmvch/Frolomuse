package com.frolo.muse.billing


sealed class TrialStatus {
    /**
     * The trial is not available at all.
     */
    object NotAvailable: TrialStatus()

    /**
     * The trial is available but has not been activated yet.
     */
    data class Available(val durationMillis: Long): TrialStatus()

    /**
     * The trial is activated.
     */
    object Activated: TrialStatus()

    /**
     * The trial was activated earlier, but now it has expired.
     */
    object Expired: TrialStatus();
}