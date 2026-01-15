package com.frolo.muse.audius.di

import com.frolo.muse.audius.repository.AudiusRepository
import com.frolo.muse.audius.usecase.SearchAudiusTracksUseCase
import dagger.Module
import dagger.Provides

@Module
class AudiusBindingModule {

    @Provides
    fun provideSearchAudiusTracksUseCase(
        repository: AudiusRepository,
        schedulerProvider: SchedulerProvider
    ): SearchAudiusTracksUseCase {
        return SearchAudiusTracksUseCase(repository, schedulerProvider)
    }
}
