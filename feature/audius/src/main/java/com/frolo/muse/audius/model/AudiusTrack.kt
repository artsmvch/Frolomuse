package com.frolo.muse.audius.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AudiusTrack(
    val id: String,
    @SerializedName("track_id")
    val trackId: Int,
    val title: String,
    val description: String?,
    val duration: Int,
    val genre: String?,
    val mood: String?,
    val releaseDate: String?,
    val artwork: AudiusArtwork?,
    val user: AudiusUser,
    val playCount: Int,
    val favoriteCount: Int,
    val repostCount: Int,
    val tags: String?,
    val permalink: String,
    val isStreamable: Boolean
): Serializable

data class AudiusArtwork(
    val `150x150`: String?,
    val `480x480`: String?,
    val `1000x1000`: String?
): Serializable

data class AudiusUser(
    val id: String,
    val name: String,
    val handle: String,
    val isVerified: Boolean,
    val profilePicture: AudiusArtwork?,
    val followerCount: Int,
    val followeeCount: Int
): Serializable

data class AudiusSearchResponse(
    val data: List<AudiusTrack>
): Serializable
