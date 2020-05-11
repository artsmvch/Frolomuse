package com.frolo.muse.ui.main.library.albums

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.R
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Album
import com.frolo.muse.ui.getNumberOfTracksString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.checkable.CheckView
import com.frolo.muse.views.media.MediaConstraintLayout
import com.google.android.material.card.MaterialCardView
import com.l4digital.fastscroll.FastScroller


class AlbumAdapter constructor(
    private val requestManager: RequestManager
): BaseAdapter<Album, BaseAdapter.BaseViewHolder>(AlbumItemCallback), FastScroller.SectionIndexer {

    companion object {
        const val VIEW_TYPE_BIG_ITEM = 0
        const val VIEW_TYPE_SMALL_ITEM = 1
    }

    var itemViewType = VIEW_TYPE_SMALL_ITEM
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int) = itemViewType

    override fun getItemId(position: Int) = getItemAt(position).id

    override fun getSectionText(position: Int) = sectionIndexAt(position) { name }

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {

        VIEW_TYPE_SMALL_ITEM -> SmallItemViewHolder(parent.inflateChild(R.layout.item_album_card))

        VIEW_TYPE_BIG_ITEM -> BigItemViewHolder(parent.inflateChild(R.layout.item_album))

        else -> throw IllegalArgumentException("Unexpected item view type: $itemViewType")
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
        item: Album,
        selected: Boolean,
        selectionChanged: Boolean
    ) {

        when (holder) {
            is SmallItemViewHolder -> {
                with(holder) {
                    requestManager.makeRequest(item.id)
                        .placeholder(R.drawable.ic_framed_album)
                        .error(R.drawable.ic_framed_album)
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .centerCrop()
                        .into(imageAlbumArt)

                    textAlbumName.text = item.name
                    textArtistName.text = item.artist

                    imageCheck.setChecked(selected, selectionChanged)
                }
            }

            is BigItemViewHolder -> {
                with(holder) {
                    requestManager.makeRequest(item.id)
                        .placeholder(R.drawable.ic_framed_album)
                        .error(R.drawable.ic_framed_album)
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .centerCrop()
                        .into(imageAlbumArt)

                    textAlbumName.text = item.name
                    textArtistName.text = item.artist
                    textNumberOfTracks.text = item.getNumberOfTracksString(itemView.resources)

                    imageCheck.setChecked(selected, selectionChanged)

                    (itemView as MediaConstraintLayout).setChecked(selected)
                }
            }

            else -> throw IllegalArgumentException("Unknown holder: $holder")
        }
    }

    class SmallItemViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        val cardView: MaterialCardView = itemView as MaterialCardView
        val textAlbumName: TextView = itemView.findViewById(R.id.tv_album_name)
        val textArtistName: TextView = itemView.findViewById(R.id.tv_artist_name)
        val imageAlbumArt: ImageView = itemView.findViewById(R.id.imv_album_art)
        val imageCheck: CheckView = itemView.findViewById(R.id.imv_check)
        override val viewOptionsMenu: View = itemView.findViewById(R.id.view_options_menu)
    }

    class BigItemViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        val textAlbumName: TextView = itemView.findViewById(R.id.tv_album_name)
        val textArtistName: TextView = itemView.findViewById(R.id.tv_artist_name)
        val textNumberOfTracks: TextView = itemView.findViewById(R.id.tv_number_of_tracks)
        val imageAlbumArt: ImageView = itemView.findViewById(R.id.imv_album_art)
        val imageCheck: CheckView = itemView.findViewById(R.id.imv_check)
        override val viewOptionsMenu: View = itemView.findViewById(R.id.view_options_menu)
    }

    object AlbumItemCallback: DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }

    }
}