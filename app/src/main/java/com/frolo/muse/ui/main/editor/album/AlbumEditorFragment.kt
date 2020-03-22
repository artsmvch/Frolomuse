package com.frolo.muse.ui.main.editor.album

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.R
import com.frolo.muse.UriPathDetector
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.model.media.Album
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.views.Anim
import kotlinx.android.synthetic.main.dialog_album_editor.*
import kotlin.math.min


class AlbumEditorFragment : BaseDialogFragment() {

    companion object {
        private const val REQUEST_PICK_IMAGE = 1337

        private const val ARG_ALBUM = "album"

        // Factory
        fun newInstance(album: Album) = AlbumEditorFragment()
                .withArg(ARG_ALBUM, album)
    }

    private val viewModel: AlbumEditorViewModel by lazy {
        val album = requireArguments().getSerializable(ARG_ALBUM) as Album
        val vmFactory = AlbumEditorVMFactory(requireApp().appComponent, album)
        ViewModelProviders.of(this, vmFactory)
                .get(AlbumEditorViewModel::class.java)
    }

    private val album: Album by serializableArg(ARG_ALBUM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkReadPermissionFor {
            viewModel.onActive()
        }

        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_album_editor)

            val metrics = resources.displayMetrics
            val height = metrics.heightPixels
            val width = metrics.widthPixels
            val min = 14 * min(height, width) / 15
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setupDialogSize(this, ViewGroup.LayoutParams.WRAP_CONTENT, min)
            } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setupDialogSize(this, min, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            initUI(this)
        }
    }

    private fun initUI(dialog: Dialog) {
        dialog.apply {
            btn_cancel.setOnClickListener { dismiss() }

            btn_save.setOnClickListener {
                checkWritePermissionFor {
                    viewModel.onSaveClicked()
                }
            }

            imv_album_art.setOnClickListener {
                startPickImageFile()
            }

            btn_delete_art.setOnClickListener {
                viewModel.onDeleteClicked()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val filepath = data.data?.let {
                    uri -> UriPathDetector.detect(context, uri)
                }
                viewModel.onFileSelected(filepath)
            }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            error.observeNonNull(owner) { err ->
                postError(err)
                dismiss()
            }

            isLoadingUpdate.observeNonNull(owner) { isLoading ->
                onSetLoading(isLoading)
            }

            updatedEvent.observeNonNull(owner) { event ->
                onAlbumUpdated(event.album, event.artChanged, event.newFilepath)
            }

            albumArtConfig.observeNonNull(owner) { config ->
                val id: Long? = config.id
                val filepath: String? = config.data
                when {
                    id != null -> loadAlbumArt(id)
                    filepath != null -> loadImage(filepath)
                    else -> loadPlaceholder()
                }
            }
        }
    }

    private fun startPickImageFile() {
        val context = requireContext()
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.pick_image)),
                    REQUEST_PICK_IMAGE)
        }
    }

    private fun loadAlbumArt(albumId: Long) {
        dialog?.apply {
            val uri = GlideAlbumArtHelper.getUri(albumId)
            val options = GlideAlbumArtHelper.get()
                    .makeRequestOptions(albumId)
                    .dontAnimate()
                    .centerCrop()
            Glide.with(this@AlbumEditorFragment)
                    .asBitmap()
                    .load(uri)
                    .apply(options)
                    .into(imv_album_art)
        }
    }

    private fun loadImage(filepath: String) {
        dialog?.apply {
            Glide.with(this@AlbumEditorFragment)
                .load(filepath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imv_album_art)
        }
    }

    private fun loadPlaceholder() {
        dialog?.apply {
            // do NOT load it using Glide. It doesn't work sometimes.
            // I can't figure out why this shit happens.
            throw UnsupportedOperationException("Not implemented")
            //imv_album_art.setImageResource(R.drawable.vector_note_square)
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        dialog?.apply {
            if (isLoading) {
                Anim.fadeIn(inc_overlay)
            } else {
                Anim.fadeOut(inc_overlay)
            }
        }
    }

    private fun onAlbumUpdated(album: Album, artChanged: Boolean, filepath: String?) {
        if (artChanged) {
            // Important to invalidate the key!
            // Fuckin' glide is not able to do it itself.
            GlideAlbumArtHelper.get().invalidate(album.id)
        }
        dismiss()
    }
}