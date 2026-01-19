package com.frolo.muse.audius.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AudiusTrack(
    @SerializedName("id")
    val id: String,

    @SerializedName("track_id")
    val trackId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("duration")
    val duration: Int,

    @SerializedName("genre")
    val genre: String?,

    @SerializedName("mood")
    val mood: String?,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("artwork")
    val artwork: AudiusArtwork?,

    @SerializedName("user")
    val user: AudiusUser?,

    @SerializedName("play_count")
    val playCount: Int,

    @SerializedName("favorite_count")
    val favoriteCount: Int,

    @SerializedName("repost_count")
    val repostCount: Int,

    @SerializedName("tags")
    val tags: String?,

    @SerializedName("permalink")
    val permalink: String,

    @SerializedName("is_streamable")
    val isStreamable: Boolean
) : Serializable


data class AudiusArtwork(
    @SerializedName("150x150")
    val `150x150`: String?,

    @SerializedName("480x480")
    val `480x480`: String?,

    @SerializedName("1000x1000")
    val `1000x1000`: String?
) : Serializable


data class AudiusUser(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("handle")
    val handle: String,

    @SerializedName("is_verified")
    val isVerified: Boolean,

    @SerializedName("profile_picture")
    val profilePicture: AudiusArtwork?,

    @SerializedName("follower_count")
    val followerCount: Int,

    @SerializedName("followee_count")
    val followeeCount: Int
) : Serializable


data class AudiusSearchResponse(
    @SerializedName("data")
    val data: List<AudiusTrack>
) : Serializable
