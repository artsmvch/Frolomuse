package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.di.Exec
import com.frolo.muse.di.impl.misc.MainExecutor
import com.frolo.muse.di.impl.permission.PermissionCheckerImpl
import com.frolo.muse.permission.PermissionChecker
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
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
    fun providePermissionChecker(context: Context): PermissionChecker {
        return PermissionCheckerImpl(context)
    }

}