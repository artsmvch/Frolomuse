package com.frolo.muse.ui.main.player.poster

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.views.Anim
import kotlinx.android.synthetic.main.dialog_poster.*
import kotlin.math.min


class PosterDialog: BaseDialogFragment() {

    private val viewModel: PosterViewModel by lazy {
        val song = requireArguments().getSerializable(ARG_SONG) as Song
        val vmFactory = requireFrolomuseApp()
            .appComponent
            .providePosterVMFactoryCreator()
            .create(song)

        ViewModelProviders.of(this, vmFactory)
                .get(PosterViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_poster)

            val metrics = resources.displayMetrics
            val height = metrics.heightPixels
            val width = metrics.widthPixels
            val min = 14 * min(height, width) / 15
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setupDialogSize(this, ViewGroup.LayoutParams.WRAP_CONTENT, min)
            } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setupDialogSize(this, min, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        btn_cancel.setOnClickListener {
            viewModel.onCancelClicked()
        }

        btn_share.setOnClickListener {
            viewModel.onShareClicked()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            postError(err)
        }

        isCreatingPoster.observeNonNull(owner) { isCreating ->
            dialog?.apply {
                if (isCreating) {
                    Anim.fadeIn(pb_loading)
                } else {
                    Anim.fadeOut(pb_loading)
                }
            }
        }

        poster.observeNonNull(owner) { bmp ->
            dialog?.apply {
                // NOTE: avoid caching the bitmap in the memory cache and on the disk.
                // Otherwise Glide will crash trying to get use the bitmap after the view model recycles it.
                Glide.with(this@PosterDialog)
                    .load(bmp)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(
                        object : CustomTarget<Drawable>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                imv_poster.setImageDrawable(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) = Unit
                        }
                    )
            }
        }

    }

    companion object {
        private const val ARG_SONG = "song"

        // Factory
        fun newInstance(song: Song) = PosterDialog()
                .withArg(ARG_SONG, song)
    }

}