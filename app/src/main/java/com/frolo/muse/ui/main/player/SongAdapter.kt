package com.frolo.muse.ui.main.player

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.frolo.muse.GlideManager
import com.frolo.muse.R
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.model.media.Song
import kotlinx.android.synthetic.main.include_square_album_art.view.*


class SongAdapter constructor(
        private val requestManager: RequestManager
): RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var queue: SongQueue? = null

    fun submitQueue(queue: SongQueue?) {
        val callback = SongQueueCallback(this.queue, queue)
        val diffResult = DiffUtil.calculateDiff(callback)
        this.queue = queue
        diffResult.dispatchUpdatesTo(this)
    }

    fun getItemAt(position: Int) = queue?.getItemAt(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.include_square_album_art, parent, false)
        return SongViewHolder(view)
    }

    override fun getItemCount(): Int = queue?.length ?: 0

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val item = getItemAt(position)
        holder.bind(item)
    }

    inner class SongViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
            RequestListener<Drawable> {

        override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean): Boolean {

            with(itemView) {
                pb_loading.visibility = View.INVISIBLE
                cv_album_art.visibility = View.VISIBLE
            }
            return false
        }

        override fun onResourceReady(
                resource: Drawable?,
                model: Any?, target:
                Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean): Boolean {

            with(itemView) {
                pb_loading.visibility = View.INVISIBLE
                cv_album_art.visibility = View.VISIBLE
            }
            return false
        }

        fun bind(item: Song?) {
            with(itemView) {
                pb_loading.visibility = View.VISIBLE

                val albumId = item?.albumId ?: -1

                val options = GlideManager.get()
                        .requestOptions(albumId)
                        .placeholder(null)
                        .error(R.drawable.ic_album_art_large_placeholder)
                val uri = GlideManager.albumArtUri(albumId)

                cv_album_art.visibility = View.INVISIBLE

                requestManager.load(uri)
                        .apply(options)
                        .addListener(this@SongViewHolder)
                        .transition(DrawableTransitionOptions().crossFade())
                        .into(imv_album_art)
            }
        }
    }

    private class SongQueueCallback constructor(
            private val oldQueue: SongQueue?,
            private val newQueue: SongQueue?
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldQueue!!.getItemAt(oldItemPosition).id ==
                    newQueue!!.getItemAt(newItemPosition).id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldQueue!!.getItemAt(oldItemPosition).albumId ==
                    newQueue!!.getItemAt(newItemPosition).albumId
        }

        override fun getOldListSize() = oldQueue?.length ?: 0

        override fun getNewListSize() = newQueue?.length ?: 0

    }
}
