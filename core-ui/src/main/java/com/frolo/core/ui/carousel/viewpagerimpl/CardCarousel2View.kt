package com.frolo.core.ui.carousel.viewpagerimpl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.frolo.core.ui.R
import com.frolo.core.ui.carousel.ViewHolderImpl
import com.frolo.ui.Screen
import com.google.android.material.card.MaterialCardView


internal class CardCarousel2View @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ViewPagerLayout<CardCarousel2View.AdapterImpl>(context, attrs, defStyleAttr) {

    override fun onCreatePagerAdapter(): AdapterImpl {
        return AdapterImpl(Glide.with(this))
    }

    internal class AdapterImpl(
        private val requestManager: RequestManager
    ): Adapter() {
        override fun onCreateViewHolder(container: ViewGroup): ViewHolderImpl {
            val layoutInflater = LayoutInflater.from(container.context)
            val itemView = layoutInflater.inflate(R.layout.carousel_item_card,
                container, false)
            itemView.findViewById<MaterialCardView>(R.id.cv_art_container).also { cardView ->
                cardView.updateLayoutParams<MarginLayoutParams> {
                    setMargins(Screen.dp(cardView.context, 48))
                }
                cardView.radius = Screen.dpFloat(cardView.context, 36f)
                cardView.cardElevation = Screen.dpFloat(cardView.context,8f)
            }
            return ViewHolderImpl(
                view = itemView,
                imageContainer = itemView.findViewById(R.id.imv_art),
                imageView = itemView.findViewById(R.id.imv_art),
                progressBar = itemView.findViewById(R.id.pb_loading),
                requestManager = requestManager
            )
        }
    }
}