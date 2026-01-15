package com.frolo.muse.audius.api

import com.frolo.muse.audius.model.AudiusSearchResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface AudiusApiService {
    
    @GET("v1/tracks/search")
    fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String = "Frolomuse"
    ): Single<AudiusSearchResponse>
}
