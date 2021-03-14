package com.frolo.muse.ui.main.editor.album

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.R
import com.frolo.muse.UriPathDetector
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Album
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.views.Anim
import kotlinx.android.synthetic.main.dialog_album_editor.*
import kotlinx.android.synthetic.main.dialog_album_editor.btn_cancel
import kotlinx.android.synthetic.main.include_album_art_deletion_confirmation.*
import kotlin.math.min


class AlbumEditorDialog : BaseDialogFragment() {

    private val viewModel: AlbumEditorViewModel by lazy {
        val album = requireArguments().getSerializable(ARG_ALBUM) as Album
        val vmFactory = AlbumEditorVMFactory(requireApp().appComponent, album)
        ViewModelProviders.of(this, vmFactory)
                .get(AlbumEditorViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        include_album_art_deletion_confirmation.setOnTouchListener { _, _ -> true }

        include_progress_overlay.setOnTouchListener { _, _ -> true }

        btn_cancel.setOnClickListener {
            dismiss()
        }

        btn_save.setOnClickListener {
            checkWritePermissionFor {
                viewModel.onSaveClicked()
            }
        }

        btn_placeholder_pick_image.setOnClickListener {
            viewModel.onPickArtOptionClicked()
        }

        imv_album_art.setOnClickListener {
            viewModel.onPickArtOptionClicked()
        }

        btn_pick_art.setOnClickListener {
            viewModel.onPickArtOptionClicked()
        }

        btn_delete_art.setOnClickListener {
            viewModel.onDeleteArtClicked()
        }

        btn_confirm_art_deletion.setOnClickListener {
            checkWritePermissionFor {
                viewModel.onArtDeletionConfirmed()
            }
        }

        btn_cancel_art_deletion.setOnClickListener {
            viewModel.onArtDeletionCanceled()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val filepath = data.data?.let {
                    uri -> UriPathDetector.detect(context, uri)
                }
                viewModel.onArtPicked(filepath)
            }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            postError(err)
        }

        pickArtEvent.observe(owner) {
            pickImage()
        }

        artVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                imv_album_art.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
            }
        }

        placeholderVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                group_placeholder.isVisible = isVisible
            }
        }

        placeholderPickArtOptionVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                btn_placeholder_pick_image.isVisible = isVisible
            }
        }

        saveArtOptionVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                btn_save.isVisible = isVisible
            }
        }

        pickArtOptionVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                btn_pick_art.isVisible = isVisible
            }
        }

        deleteArtOptionVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                btn_delete_art.isVisible = isVisible
            }
        }

        artDeletionConfirmationVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                if (isVisible) {
                    Anim.fadeIn(include_album_art_deletion_confirmation)
                } else {
                    Anim.fadeOut(include_album_art_deletion_confirmation)
                }
            }
        }

        isSavingChanges.observeNonNull(owner) { isSaving ->
            dialog?.apply {
                if (isSaving) {
                    Anim.fadeIn(include_progress_overlay)
                } else {
                    Anim.fadeOut(include_progress_overlay)
                }
            }
        }

        artUpdatedEvent.observeNonNull(owner) { album ->
            onAlbumUpdated(album)
        }

        art.observe(owner) { bitmap ->
            dialog?.apply {
                Glide.with(this@AlbumEditorDialog)
                        .load(bitmap)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imv_album_art)
            }
        }
    }

    private fun pickImage() {
        val context = requireContext()
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            val chooser = Intent.createChooser(intent, getString(R.string.pick_image))
            startActivityForResult(chooser, REQUEST_PICK_IMAGE)
        }
    }

    private fun onAlbumUpdated(album: Album) {
        context?.also { safeContext ->
            AlbumUpdateEvent.dispatch(safeContext, album, album)
        }
        dismiss()
    }

    companion object {
        private const val REQUEST_PICK_IMAGE = 1337

        private const val ARG_ALBUM = "album"

        // Factory
        fun newInstance(album: Album) = AlbumEditorDialog()
                .withArg(ARG_ALBUM, album)
    }

}