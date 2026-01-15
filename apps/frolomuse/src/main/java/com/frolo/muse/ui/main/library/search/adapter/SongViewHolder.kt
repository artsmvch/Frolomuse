package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.frolo.muse.R
import com.frolo.muse.audius.model.AudiusSong
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.views.MiniVisualizer
import com.frolo.muse.views.checkable.CheckView
import com.bumptech.glide.Glide


class SongViewHolder constructor(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)

    private val tvSongName: TextView = itemView.findViewById(R.id.tv_song_name)
    private val tvArtistName: TextView = itemView.findViewById(R.id.tv_artist_name)
    private val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
    private val tvAudiusBadge: TextView = itemView.findViewById(R.id.tv_audius_badge)
    private val imvSongThumbnail: ImageView = itemView.findViewById(R.id.imv_song_thumbnail)
    private val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)
    private val miniVisualizer: MiniVisualizer = itemView.findViewById(R.id.mini_visualizer)

    init {
        miniVisualizer.visibility = View.GONE
    }

    fun bind(
        item: Song,
        selected: Boolean,
        selectionChanged: Boolean,
        query: String
    ) {

        with(itemView) {
            val res = resources
            tvSongName.text = highlight(text = item.getNameString(resources), part = query)
            tvArtistName.text = item.getAlbumString(res)
            tvDuration.text = item.getDurationString()

            // Show/hide Audius badge
            if (item is AudiusSong) {
                tvAudiusBadge.visibility = View.VISIBLE
                // Load Audius artwork using Glide
                val artworkUrl = item.getArtworkUrl()
                if (artworkUrl != null) {
                    Glide.with(context)
                        .load(artworkUrl)
                        .placeholder(R.drawable.widget_album_art_placeholder)
                        .error(R.drawable.widget_album_art_placeholder)
                        .into(imvSongThumbnail)
                } else {
                    thumbnailLoader.loadSongThumbnail(item, imvSongThumbnail)
                }
            } else {
                tvAudiusBadge.visibility = View.GONE
                thumbnailLoader.loadSongThumbnail(item, imvSongThumbnail)
            }

            imvCheck.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}