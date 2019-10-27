package com.frolo.muse.ui.main.library.artists.artist.albums

import android.content.ContentUris
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import com.frolo.muse.GlideManager
import com.frolo.muse.R
import com.frolo.muse.model.media.Album
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_album_of_artist.view.*


class AlbumOfArtistAdapter constructor(
        private val requestManager: RequestManager
): BaseAdapter<Album, AlbumOfArtistAdapter.AlbumViewHolder>(AlbumOfArtistItemCallback) {

    override fun getItemId(position: Int) = getItemAt(position).id

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_album_of_artist, parent, false)

        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(
            holder: AlbumViewHolder,
            position: Int,
            item: Album,
            selected: Boolean,
            selectionChanged: Boolean) {

        with(holder.itemView) {
            val res = resources

            tv_album_name.text = item.getNameString(res)

            val albumId = item.id
            val options = GlideManager.get()
                    .requestOptions(albumId)
                    .placeholder(R.drawable.vector_note_square)
                    .error(R.drawable.vector_note_square)
            requestManager.load(ContentUris.withAppendedId(GlideManager.albumArtUri(), albumId))
                    .apply(options)
                    .into(imv_album_art)

            imv_check.setChecked(selected, selectionChanged)
        }
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