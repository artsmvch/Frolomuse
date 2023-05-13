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
            "R.drawable.ic_player_notification_small", Integer.toHexString(R.drawable.ic_player_notification_small), "0x7f0800f1"
        )
    }

    private fun checkResId(resName: String, actualHexValue: String, expectedHexValue: String) {
        if (actualHexValue != expectedHexValue) {
            throw IllegalStateException("Generated value for $resName resource has changed since the last build: " +
                    "the actual value $actualHexValue and but expected $expectedHexValue")
        }
    }
}