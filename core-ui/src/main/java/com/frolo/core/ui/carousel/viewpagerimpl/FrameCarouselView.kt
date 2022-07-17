package com.frolo.core.ui.carousel.viewpagerimpl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.frolo.core.ui.R
import com.frolo.core.ui.carousel.ICarouselView
import com.frolo.core.ui.carousel.ViewHolderImpl


internal class FrameCarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?= null,
    defStyleAttr: Int = 0
): ViewPagerLayout<FrameCarouselView.AdapterImpl>(context, attrs, defStyleAttr), ICarouselView {

    override fun onCreatePagerAdapter(): AdapterImpl {
        return AdapterImpl(Glide.with(this))
    }

    internal class AdapterImpl constructor(
        private val requestManager: RequestManager
    ): Adapter() {
        override fun onCreateViewHolder(container: ViewGroup): ViewHolderImpl {
            val layoutInflater = LayoutInflater.from(container.context)
            val itemView = layoutInflater.inflate(R.layout.carousel_item_frame,
                container, false)
            return ViewHolderImpl(
                view = itemView,
                imageContainer = itemView.findViewById(R.id.imv_art),
                imageView = itemView.findViewById(R.id.imv_art),
                progressBar = itemView.findViewById(R.id.progress_bar),
                requestManager = requestManager
            )
        }
    }
}