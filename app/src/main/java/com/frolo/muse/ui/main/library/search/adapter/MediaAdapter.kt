package com.frolo.muse.ui.main.library.search.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.model.media.*
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.toPx
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import kotlinx.android.synthetic.main.item_header.view.*
import kotlin.properties.Delegates


class MediaAdapter(private val requestManager: RequestManager):
        BaseAdapter<Media, MediaAdapter.MediaViewHolder>(MediaItemCallback),
        StickyRecyclerHeadersAdapter<MediaAdapter.HeaderViewHolder> {

    var query by Delegates.observable("") { _, _, _ -> notifyDataSetChanged() }

    override fun getItemViewType(position: Int) = getItemAt(position).kind

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            Media.SONG -> SongViewHolder(
                    inflater.inflate(R.layout.item_song, parent, false))

            Media.ALBUM -> AlbumViewHolder(
                    inflater.inflate(R.layout.item_album, parent, false))

            Media.ARTIST -> ArtistViewHolder(
                    inflater.inflate(R.layout.item_artist, parent, false))

            Media.GENRE -> GenreViewHolder(
                    inflater.inflate(R.layout.item_genre, parent, false))

            Media.PLAYLIST -> PlaylistViewHolder(
                    inflater.inflate(R.layout.item_playlist, parent, false))

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(
            holder: MediaViewHolder,
            position: Int,
            item: Media,
            selected: Boolean,
            selectionChanged: Boolean) {

        when(holder) {
            is SongViewHolder -> holder.bind(item as Song, selected, selectionChanged, requestManager, query)
            is AlbumViewHolder -> holder.bind(item as Album, selected, selectionChanged, requestManager, query)
            is ArtistViewHolder -> holder.bind(item as Artist, selected, selectionChanged, query)
            is GenreViewHolder -> holder.bind(item as Genre, selected, selectionChanged, query)
            is PlaylistViewHolder -> holder.bind(item as Playlist, selected, selectionChanged, query)
        }
    }

    override fun getHeaderId(position: Int): Long = getItemViewType(position).toLong()

    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header, parent, false)
        return HeaderViewHolder(view)
    }

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
        private val highlightColor = StyleUtil.getHighlightColor(itemView.context)
        private val highlightSpan = ForegroundColorSpan(highlightColor)

        init {
            itemView.apply {
                layoutParams = layoutParams.also { params ->
                    if (params is ViewGroup.MarginLayoutParams) {
                        val margin = 2f.toPx(context).toInt()
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