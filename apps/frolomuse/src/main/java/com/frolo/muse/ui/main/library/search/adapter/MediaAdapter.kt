package com.frolo.muse.ui.main.library.search.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.*
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.music.model.*
import com.frolo.ui.Screen
import com.frolo.ui.StyleUtils
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import kotlinx.android.synthetic.main.item_header.view.*
import kotlin.properties.Delegates


class MediaAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader
): BaseAdapter<Media, MediaAdapter.MediaViewHolder>(MediaItemCallback),
        StickyRecyclerHeadersAdapter<MediaAdapter.HeaderViewHolder> {

    var query by Delegates.observable("") { _, _, _ -> notifyDataSetChanged() }

    override fun getItemViewType(position: Int) = getItemAt(position).kind

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaViewHolder = when(viewType) {
        Media.SONG -> SongViewHolder(parent.inflateChild(R.layout.item_song), thumbnailLoader)

        Media.ALBUM -> AlbumViewHolder(parent.inflateChild(R.layout.item_album), thumbnailLoader)

        Media.ARTIST -> ArtistViewHolder(parent.inflateChild(R.layout.item_artist), thumbnailLoader)

        Media.GENRE -> GenreViewHolder(parent.inflateChild(R.layout.item_genre), thumbnailLoader)

        Media.PLAYLIST -> PlaylistViewHolder(parent.inflateChild(R.layout.item_playlist), thumbnailLoader)

        else -> throw IllegalArgumentException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(
        holder: MediaViewHolder,
        position: Int,
        item: Media,
        selected: Boolean,
        selectionChanged: Boolean
    ) = when(holder.itemViewType) {
        Media.SONG ->
            (holder as SongViewHolder).bind(item as Song, selected, selectionChanged, query)

        Media.ALBUM ->
            (holder as AlbumViewHolder).bind(item as Album, selected, selectionChanged, query)

        Media.ARTIST ->
            (holder as ArtistViewHolder).bind(item as Artist, selected, selectionChanged, query)

        Media.GENRE ->
            (holder as GenreViewHolder).bind(item as Genre, selected, selectionChanged, query)

        Media.PLAYLIST ->
            (holder as PlaylistViewHolder).bind(item as Playlist, selected, selectionChanged, query)

        else -> throw IllegalArgumentException("Unexpected view type: ${holder.itemViewType}")
    }

    override fun getHeaderId(position: Int): Long = getItemViewType(position).toLong()

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderViewHolder =
            HeaderViewHolder(parent.inflateChild(R.layout.item_header))

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder, position: Int) {
        with(holder.itemView) {
            val title = when (getItemViewType(position)) {
                Media.SONG -> context.getString(R.string.songs)
                Media.ALBUM -> context.getString(R.string.albums)
                Media.ARTIST -> context.getString(R.string.artists)
                Media.GENRE -> context.getString(R.string.genres)
                Media.PLAYLIST -> context.getString(R.string.playlists)
                else -> ""
            }
            tv_header.text = title
        }
    }

    abstract class MediaViewHolder(itemView: View): BaseViewHolder(itemView) {

        @get:ColorInt
        private val highlightColor: Int by lazy {
            val textColor = StyleUtils.resolveColor(itemView.context, android.R.attr.textColorPrimary)
            val accentColor = StyleUtils.resolveColor(itemView.context,
                com.google.android.material.R.attr.colorAccent)
            ColorUtils.compositeColors(accentColor, textColor)
        }

        init {
            itemView.apply {
                layoutParams = layoutParams.also { params ->
                    if (params is ViewGroup.MarginLayoutParams) {
                        val margin = Screen.dp(context, 2f)
                        params.leftMargin = margin
                        params.topMargin = margin
                        params.rightMargin = margin
                        params.bottomMargin = margin
                    }
                }
            }
        }

        fun highlight(text: String, part: String): CharSequence {
            val index = text.indexOf(string = part, ignoreCase = true)
            return if (index >= 0) {
                val spannableString = SpannableString(text)
                spannableString.setSpan(ForegroundColorSpan(highlightColor),
                        index, index + part.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                spannableString.setSpan(StyleSpan(Typeface.BOLD),
                        index, index + part.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                return spannableString
            } else text
        }
    }

    class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    object MediaItemCallback: DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.kind == newItem.kind
                    && oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }

    }
}