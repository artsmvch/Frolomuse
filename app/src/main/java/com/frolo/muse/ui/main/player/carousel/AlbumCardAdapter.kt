package com.frolo.muse.ui.main.player.carousel

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.frolo.muse.*
import com.frolo.muse.common.albumId
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.glide.makeRequest
import kotlinx.android.synthetic.main.include_square_album_art.view.*
import kotlin.math.max


class AlbumCardAdapter constructor(
    private val requestManager: RequestManager
): ListAdapter<AudioSource, AlbumCardAdapter.AlbumArtViewHolder>(ItemDiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlbumArtViewHolder {
        val itemView = parent.inflateChild(R.layout.include_square_album_art)

        val maxCardElevation = 16f.dp2px(parent.context)
        val cornerRadius = 6f.dp2px(parent.context)

        val horizontalPadding =
                calculateCardHorizontalShadowPadding(maxCardElevation, cornerRadius)

        val verticalPadding =
                calculateCardVerticalShadowPadding(maxCardElevation, cornerRadius)

        val padding = max(horizontalPadding, verticalPadding).toInt()

        itemView.cv_album_art.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = padding
            topMargin = padding
            rightMargin = padding
            bottomMargin = padding
        }

        return AlbumArtViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlbumArtViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class AlbumArtViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView), RequestListener<Drawable> {

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {

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
            isFirstResource: Boolean
        ): Boolean {

            with(itemView) {
                pb_loading.visibility = View.INVISIBLE
                cv_album_art.visibility = View.VISIBLE
            }
            return false
        }

        fun bind(item: AudioSource?) = with(itemView) {
            pb_loading.visibility = View.VISIBLE
            cv_album_art.visibility = View.INVISIBLE

            // The error drawable is a large PNG,
            // so we need to load it through a split request
            // in order to resize correctly and avoid OOM errors.
            val errorRequest = requestManager.load(R.drawable.art_placeholder).skipMemoryCache(false)

            requestManager.makeRequest(item?.albumId ?: -1)
                .placeholder(null)
                .error(errorRequest)
                .addListener(this@AlbumArtViewHolder)
                .transition(DrawableTransitionOptions().crossFade())
                .into(imv_album_art)
        }
    }

    object ItemDiffCallback : DiffUtil.ItemCallback<AudioSource>() {

        override fun areItemsTheSame(oldItem: AudioSource, newItem: AudioSource): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AudioSource, newItem: AudioSource): Boolean {
            return oldItem.albumId == newItem.albumId
        }

    }
}
