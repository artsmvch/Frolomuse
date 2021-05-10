package com.frolo.muse.ui.main.library.search.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.model.media.*
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.dp2px
import com.frolo.muse.inflateChild
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import kotlinx.android.synthetic.main.item_header.view.*
import kotlin.properties.Delegates


class MediaAdapter constructor(
    private val requestManager: RequestManager
): BaseAdapter<Media, MediaAdapter.MediaViewHolder>(MediaItemCallback),
        StickyRecyclerHeadersAdapter<MediaAdapter.HeaderViewHolder> {

    var query by Delegates.observable("") { _, _, _ -> notifyDataSetChanged() }

    override fun getItemViewType(position: Int) = getItemAt(position).kind

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaViewHolder = when(viewType) {
        Media.SONG -> SongViewHolder(parent.inflateChild(R.layout.item_song))

        Media.ALBUM -> AlbumViewHolder(parent.inflateChild(R.layout.item_album))

        Media.ARTIST -> ArtistViewHolder(parent.inflateChild(R.layout.item_artist))

        Media.GENRE -> GenreViewHolder(parent.inflateChild(R.layout.item_genre))

        Media.PLAYLIST -> PlaylistViewHolder(parent.inflateChild(R.layout.item_playlist))

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
            (holder as SongViewHolder).bind(item as Song, selected, selectionChanged, requestManager, query)

        Media.ALBUM ->
            (holder as AlbumViewHolder).bind(item as Album, selected, selectionChanged, requestManager, query)

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

        @ColorInt
        private val primarySurfaceColor: Int =
                StyleUtil.resolveColor(itemView.context, R.attr.colorPrimarySurface)

        @ColorInt
        private val highlightColor: Int =
                ColorUtils.setAlphaComponent(primarySurfaceColor, (0.85f * 255).toInt())

        private val highlightSpan = ForegroundColorSpan(highlightColor)

        init {
            itemView.apply {
                layoutParams = layoutParams.also { params ->
                    if (params is ViewGroup.MarginLayoutParams) {
                        val margin = 2f.dp2px(context).toInt()
                        params.leftMargin = margin
                        params.topMargin = margin
                        params.marginEnd = margin
                        params.bottomMargin = margin
                    }
                }
            }
        }

        fun String.highlight(query: String): CharSequence {
            val i = indexOf(string = query, ignoreCase = true)
            return if (i >= 0) {
                val ss = SpannableString(this)
                ss.setSpan(highlightSpan, i, i + query.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                return ss
            } else this
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