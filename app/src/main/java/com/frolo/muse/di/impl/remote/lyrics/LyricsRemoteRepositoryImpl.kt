package com.frolo.muse.di.impl.remote.lyrics

import androidx.collection.LruCache
import com.frolo.muse.model.lyrics.Lyrics
import com.frolo.music.model.Song
import com.frolo.muse.repository.LyricsRemoteRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.math.min


class LyricsRemoteRepositoryImpl private constructor(
    private val enableCache: Boolean,
    private val maxCacheSize: Int
): LyricsRemoteRepository {

    private val api: LyricsApi by lazy { createLyricsApi() }
    private val cache: LruCache<CacheParams, Lyrics>? by lazy {
        if (enableCache) createLyricsCache(api) else null
    }

    private fun createLyricsCache(api: LyricsApi): LruCache<CacheParams, Lyrics> {
        return LyricsLruCache(calculateCacheMaxSize(maxCacheSize))
    }

    private fun createLyricsApi(): LyricsApi {
        val apiList = arrayListOf<LyricsApi>(
            SongLyricsApi(),
            MetroLyricsApi()
        )
        return LyricsApiChain(apiList)
    }

    override fun test(): Single<Boolean> {
        return Single.fromCallable {
            // Good data for testing
            val testArtistName = "Eminem"
            val testSongName = "Without me"
            val lyrics = api.getLyrics(testArtistName, testSongName)
            val text = lyrics.text
            // We should receive a non-blank text
            text.isNotBlank()
        }.onErrorReturnItem(false)
    }

    override fun getLyrics(song: Song): Single<Lyrics> {
        val source = Single.fromCallable {
            val cacheParams = CacheParams(song.artist, song.title)
            val cachedLyrics = cache?.get(cacheParams)
            if (cachedLyrics == null) {
                val lyrics = api.getLyrics(song.artist, song.title)
                cache?.put(cacheParams, lyrics)
                return@fromCallable lyrics
            } else {
                return@fromCallable cachedLyrics
            }
        }

        return source.subscribeOn(Schedulers.io())
    }

    private data class CacheParams(val songName: String, val artistName: String)

    private class LyricsLruCache(maxSize: Int) : LruCache<CacheParams, Lyrics>(maxSize) {
        override fun sizeOf(key: CacheParams, value: Lyrics): Int {
            return value.text.orEmpty().length
        }
    }

    companion object {

        // We count that the average lyrics of a song is 3,000 letters,
        // and we want to be able to store at least 128 lyrics.
        private const val PREFERRED_CACHE_SIZE = 128 * 3_000

        private fun calculateCacheMaxSize(desiredMaxSize: Int): Int {
            val allowedMaxSizeInBytes = 1024 * 1024 // 1 mb only allowed
            val allowedMaxSize = allowedMaxSizeInBytes / 4
            return min(desiredMaxSize, allowedMaxSize)
        }

        fun withCache(maxCacheSize: Int = PREFERRED_CACHE_SIZE): LyricsRemoteRepository {
            return LyricsRemoteRepositoryImpl(enableCache = true, maxCacheSize = maxCacheSize)
        }

        fun withoutCache(): LyricsRemoteRepository {
            return LyricsRemoteRepositoryImpl(enableCache = false, maxCacheSize = 0)
        }
    }
}