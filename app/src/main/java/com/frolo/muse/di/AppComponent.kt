package com.frolo.muse.di

import com.frolo.muse.di.modules.*
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        AppModule::class,
        PlayerModule::class,
        ViewModelModule::class,
        LocalDataModule::class,
        RemoteDataModule::class,
        RouterModule::class,
        EventLoggerModule::class,
        NetworkModule::class,
        MiscModule::class,
        UseCaseModule::class,
        BillingModule::class
    ]
)
interface AppComponent : ComponentInjector, ComponentProvider