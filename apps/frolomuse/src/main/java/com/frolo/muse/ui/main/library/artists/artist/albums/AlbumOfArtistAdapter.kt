package com.frolo.muse.ui.main.library.artists.artist.albums

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import com.frolo.core.ui.glide.makeAlbumArtRequest
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.music.model.Album
import com.frolo.ui.Screen
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_album_of_artist.view.*


class AlbumOfArtistAdapter constructor(
    private val requestManager: RequestManager
): BaseAdapter<Album, AlbumOfArtistAdapter.AlbumViewHolder>(AlbumOfArtistItemCallback) {

    override fun getItemId(position: Int) = getItemAt(position).id

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlbumViewHolder {
        val itemView = parent.inflateChild(R.layout.item_album_of_artist)
        Screen.getScreenWidth(parent.context).also { totalWidth ->
            if (totalWidth > 0) {
                itemView.updateLayoutParams {
                    width = (totalWidth / 3.5).toInt()
                }
            }
        }
        return AlbumViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: AlbumViewHolder,
        position: Int,
        item: Album,
        selected: Boolean,
        selectionChanged: Boolean
    ) = with(holder.itemView) {
        tv_album_name.text = item.getNameString(resources)

        requestManager.makeAlbumArtRequest(item.id)
            .placeholder(R.drawable.ic_framed_album)
            .error(R.drawable.ic_framed_album)
            .into(imv_album_art)

        imv_check.setChecked(selected, selectionChanged)
    }

    class AlbumViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.findViewById<View>(R.id.view_options_menu)
    }

    object AlbumOfArtistItemCallback: DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }

    }
}