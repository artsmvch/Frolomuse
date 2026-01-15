package com.frolo.muse.di.modules

import com.frolo.muse.audius.di.AudiusBindingModule
import com.frolo.muse.audius.di.AudiusModule
import dagger.Module

@Module(includes = [AudiusModule::class, AudiusBindingModule::class])
class AudiusIntegrationModule
