package com.frolo.muse

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CheckPlayStoreReleaseResIdsTest {
    @Test
    fun checkResIds() {
        // TODO: update the expected value
        checkResId(
            "R.drawable.ic_player_notification_small", R.drawable.ic_player_notification_small, 0
        )
    }

    private fun checkResId(resName: String, actualValue: Int, expectedValue: Int) {
        if (actualValue != expectedValue) {
            throw IllegalStateException("Generated value for $resName resource has changed since the last build: " +
                    "the actual value ${Integer.toHexString(actualValue)} and but expected ${Integer.toHexString(expectedValue)}")
        }
    }
}