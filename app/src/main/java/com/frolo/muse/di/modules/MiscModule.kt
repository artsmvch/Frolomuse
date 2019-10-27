package com.frolo.muse.di.modules

import com.frolo.muse.di.Exec
import com.frolo.muse.di.impl.misc.MainExecutor
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

}