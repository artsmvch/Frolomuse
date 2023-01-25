package com.frolo.teststools.assets

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

// TODO: do we need to use the GrantPermissionRule?
class TestAssetsRule internal constructor(): TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                try {
                    TestAssets.copyToExternalStorage()
                    base.evaluate()
                } finally {
                    TestAssets.removeFromExternalStorage()
                }
            }
        }
    }

    companion object {
        fun create(): TestRule {
            val grantPermissionRule = GrantPermissionRule.grant(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            val delegate = RuleChain
                .outerRule(grantPermissionRule)
                .around(TestAssetsRule())
            return delegate
        }
    }
}