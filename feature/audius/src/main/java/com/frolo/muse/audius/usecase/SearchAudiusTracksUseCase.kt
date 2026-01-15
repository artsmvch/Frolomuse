package com.frolo.muse.audius.usecase

import com.frolo.muse.audius.di.SchedulerProvider
import com.frolo.muse.audius.model.AudiusSong
import com.frolo.muse.audius.repository.AudiusRepository
import io.reactivex.Single
import javax.inject.Inject

class SearchAudiusTracksUseCase @Inject constructor(
    private val repository: AudiusRepository,
    private val schedulerProvider: SchedulerProvider
) {
    
    fun search(query: String): Single<List<AudiusSong>> {
        if (query.isBlank()) {
            return Single.just(emptyList())
        }
        
        return repository.searchTracks(query)
            .subscribeOn(schedulerProvider.worker())
    }
}
