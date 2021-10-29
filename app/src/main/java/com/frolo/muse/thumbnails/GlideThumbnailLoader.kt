package com.frolo.muse.thumbnails

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.frolo.customdrawable.squircle.SquircleColorDrawable
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.R
import com.frolo.muse.Screen
import com.frolo.muse.StyleUtil
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.glide.squircleCrop
import com.frolo.muse.model.media.*
import com.frolo.muse.repository.AppearancePreferences


@UiThread
class GlideThumbnailLoader constructor(
    private val fragment: Fragment
) : ThumbnailLoader {

    // Must only be lazily
    private val requestManager: RequestManager by lazy { Glide.with(fragment) }

    private val appearancePreferences: AppearancePreferences by lazy {
        FrolomuseApp.from(fragment.requireContext())
            .appComponent
            .provideAppearancePreferences()
    }

    // Cache
    private val thumbnailCache = DrawableTransformationCache()

    private fun getThumbnailFromResource(context: Context, @DrawableRes drawableId: Int): Drawable? {
        val cachedValue = thumbnailCache.get(context, drawableId)
        if (cachedValue != null) {
            return cachedValue
        }

        val raw = ContextCompat.getDrawable(context, drawableId)
                ?: return null
        val thumbnail = createLayeredThumbnail(context, raw)
        thumbnailCache.put(context, drawableId, thumbnail)

        return thumbnail
    }

    private fun createLayeredThumbnail(context: Context, thumbnail: Drawable): Drawable {
        val backgroundColor = StyleUtil.resolveColor(context, R.attr.thumbnailBackgroundTint)
        val background = SquircleColorDrawable(SQUIRCLE_CURVATURE, backgroundColor)

        val foregroundColor = StyleUtil.resolveColor(context, R.attr.thumbnailForegroundTint)
        val coloredThumbnail = thumbnail.mutate().apply {
            setTint(foregroundColor)
        }

        val layerDrawable = LayerDrawable(arrayOf(background, coloredThumbnail))
        val thumbnailInset = Screen.dp(context, 12)
        layerDrawable.setLayerInset(1, thumbnailInset, thumbnailInset, thumbnailInset, thumbnailInset)

        return layerDrawable
    }

    private fun loadThumbnailFromResource(target: ImageView, @DrawableRes what: Int) {
        val thumbnail = getThumbnailFromResource(target.context, what)
        target.setImageDrawable(thumbnail)
    }

    override fun loadSongThumbnail(song: Song, target: ImageView) {
        val placeholder = getThumbnailFromResource(target.context, R.drawable.ic_framed_music_note)
        requestManager.makeRequest(song.albumId)
            .placeholder(placeholder)
            .error(placeholder)
            .squircleCrop(SQUIRCLE_CURVATURE)
            .into(target)
    }

    override fun loadAlbumThumbnail(album: Album, target: ImageView) {
        requestManager.makeRequest(album.id)
            .placeholder(R.drawable.ic_framed_album)
            .error(R.drawable.ic_framed_album)
            .into(target)
    }

    override fun loadRawAlbumThumbnail(album: Album, target: ImageView) {
    }

    override fun loadArtistThumbnail(artist: Artist, target: ImageView) {
        loadThumbnailFromResource(target, R.drawable.ic_framed_artist)
    }

    override fun loadGenreThumbnail(genre: Genre, target: ImageView) {
        loadThumbnailFromResource(target, R.drawable.ic_framed_genre)
    }

    override fun loadPlaylistThumbnail(playlist: Playlist, target: ImageView) {
        loadThumbnailFromResource(target, R.drawable.ic_framed_playlist)
    }

    override fun loadMyFileThumbnail(file: MyFile, target: ImageView) {
        when {
            file.isDirectory -> {
                requestManager.load(R.drawable.ic_framed_folder).into(target)
            }
            file.isSongFile -> {
                loadThumbnailFromResource(target, R.drawable.ic_framed_music_note)
            }
            else -> {
                val nullSource: String? = null
                requestManager.load(nullSource).into(target)
            }
        }
    }

    override fun loadMediaFileThumbnail(file: MediaFile, target: ImageView) {
        loadThumbnailFromResource(target, R.drawable.ic_framed_music_note)
    }

    companion object {
        private const val SQUIRCLE_CURVATURE = 3.0
        private const val THUMBNAIL_BACKGROUND_ALPHA: Int = (255 * 0.32f).toInt()
    }
}