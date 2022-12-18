package com.frolo.muse

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.frolo.muse.di.ApplicationComponent
import com.frolo.muse.di.ApplicationComponentHolder
import com.frolo.muse.di.DaggerApplicationComponent
import com.frolo.muse.di.modules.*


class FrolomuseApp : MultiDexApplication(),
    ApplicationComponentHolder {

    override val applicationComponent: ApplicationComponent by lazy { buildApplicationComponent() }

    override fun onCreate() {
        super.onCreate()
        applicationComponent.provideAppStartUpInitializer().init()
    }

    private fun buildApplicationComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .localDataModule(LocalDataModule())
            .remoteDataModule(RemoteDataModule())
            .miscModule(MiscModule())
            .billingModule(BillingModule(BuildInfo.isDebug()))
            .build()
    }

    companion object {
        fun from(context: Context): FrolomuseApp {
            val applicationContext: Context = context.applicationContext
            if (applicationContext !is FrolomuseApp) {
                throw NullPointerException("Application context is not an instance " +
                        "of ${FrolomuseApp::class.java.simpleName}")
            }
            return applicationContext
        }
    }

}