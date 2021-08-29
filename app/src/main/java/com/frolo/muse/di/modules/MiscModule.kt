package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.ActivityWatcher
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.billing.BillingManager
import com.frolo.muse.di.Exec
import com.frolo.muse.di.impl.misc.MainExecutor
import com.frolo.muse.di.impl.permission.PermissionCheckerImpl
import com.frolo.muse.permission.PermissionChecker
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton


@Module
class MiscModule {

    @Singleton
    @Provides
    @Exec(Exec.Type.MAIN)
    fun provideMainExecutor(): Executor {
        return MainExecutor()
    }

    @Singleton
    @Provides
    @Exec(Exec.Type.QUERY)
    fun provideQueryExecutor(): Executor {
        return Executors.newCachedThreadPool()
    }

    @Provides
    fun provideActivityWatcher(app: FrolomuseApp): ActivityWatcher {
        return app
    }

    @Singleton
    @Provides
    fun providePermissionChecker(context: Context, activityWatcher: ActivityWatcher): PermissionChecker {
        return PermissionCheckerImpl(context, activityWatcher)
    }

    @Singleton
    @Provides
    fun provideBillingManager(app: FrolomuseApp): BillingManager {
        return BillingManager(app)
    }

}