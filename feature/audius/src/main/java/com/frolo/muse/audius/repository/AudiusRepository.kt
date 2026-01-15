package com.frolo.muse.audius.repository

import com.frolo.muse.audius.api.AudiusApiService
import com.frolo.muse.audius.model.AudiusSong
import com.frolo.muse.audius.model.AudiusTrack
import io.reactivex.Single
import javax.inject.Inject

interface AudiusRepository {
    fun searchTracks(query: String): Single<List<AudiusSong>>
}

class AudiusRepositoryImpl @Inject constructor(
    private val apiService: AudiusApiService
) : AudiusRepository {

    override fun searchTracks(query: String): Single<List<AudiusSong>> {
        return apiService.searchTracks(query)
            .map { response ->
                response.data.map { track ->
                    AudiusSong(track)
                }
            }
    }
}
