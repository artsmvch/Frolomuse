package com.frolo.core.ui.carousel.viewpager2impl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.frolo.core.ui.R
import com.frolo.core.ui.carousel.ViewHolderImpl
import com.frolo.player.AudioSource
import com.google.android.material.card.CardViewSupport
import kotlin.math.max


internal class CardCarouselAdapter constructor(
    private val requestManager: RequestManager
): ListAdapter<AudioSource, CardCarouselAdapter.ViewHolder>(ItemDiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.carousel_item_card, parent, false)

        val maxCardElevation = CardProperties.getMaxCardElevation(itemView.context)
        val cornerRadius = CardProperties.getCardCornerRadius(itemView.context)

        val horizontalPadding: Float =
                CardViewSupport.calculateCardHorizontalShadowPadding(maxCardElevation, cornerRadius)

        val verticalPadding: Float =
                CardViewSupport.calculateCardVerticalShadowPadding(maxCardElevation, cornerRadius)

        val padding = max(horizontalPadding, verticalPadding).toInt()

        itemView.findViewById<View>(R.id.cv_art_container)
            .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = padding
                topMargin = padding
                rightMargin = padding
                bottomMargin = padding
            }

        return ViewHolder(itemView, requestManager)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.recycle()
    }

    class ViewHolder(
        itemView: View,
        requestManager: RequestManager
    ): RecyclerView.ViewHolder(itemView) {

        private val holderImpl = ViewHolderImpl(
            view = itemView,
            imageContainer = itemView.findViewById(R.id.cv_art_container),
            imageView = itemView.findViewById(R.id.imv_art),
            progressBar = itemView.findViewById(R.id.pb_loading),
            requestManager = requestManager
        )

        fun bind(item: AudioSource?) {
            holderImpl.bind(item)
        }

        fun recycle() {
            holderImpl.recycle()
        }
    }

    object ItemDiffCallback : DiffUtil.ItemCallback<AudioSource>() {
        override fun areItemsTheSame(oldItem: AudioSource, newItem: AudioSource): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AudioSource, newItem: AudioSource): Boolean {
            return oldItem.metadata.albumId == newItem.metadata.albumId
        }
    }
}
