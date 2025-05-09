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
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.DialogAlbumEditorBinding
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.Album
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.views.Anim
import kotlin.math.min


class AlbumEditorDialog : BaseDialogFragment() {
    private var _binding: DialogAlbumEditorBinding? = null
    private val binding: DialogAlbumEditorBinding get() = _binding!!

    private val viewModel: AlbumEditorViewModel by lazy {
        val album = requireArguments().getSerializable(ARG_ALBUM) as Album
        val vmFactory = AlbumEditorVMFactory(activityComponent, album)
        ViewModelProviders.of(this, vmFactory)
            .get(AlbumEditorViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            _binding = DialogAlbumEditorBinding.inflate(layoutInflater)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        binding.includeAlbumArtDeletionConfirmation.root.setOnTouchListener { _, _ -> true }

        binding.includeProgressOverlay.root.setOnTouchListener { _, _ -> true }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            checkWritePermissionFor {
                viewModel.onSaveClicked()
            }
        }

        binding.btnPlaceholderPickImage.setOnClickListener {
            viewModel.onPickArtOptionClicked()
        }

        binding.imvAlbumArt.setOnClickListener {
            viewModel.onPickArtOptionClicked()
        }

        binding.btnPickArt.setOnClickListener {
            viewModel.onPickArtOptionClicked()
        }

        binding.btnDeleteArt.setOnClickListener {
            viewModel.onDeleteArtClicked()
        }

        binding.includeAlbumArtDeletionConfirmation.btnConfirmArtDeletion.setOnClickListener {
            checkWritePermissionFor {
                viewModel.onArtDeletionConfirmed()
            }
        }

        binding.includeAlbumArtDeletionConfirmation.btnCancelArtDeletion.setOnClickListener {
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
                binding.imvAlbumArt.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
            }
        }

        placeholderVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                binding.groupPlaceholder.isVisible = isVisible
            }
        }

        placeholderPickArtOptionVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                binding.btnPlaceholderPickImage.isVisible = isVisible
            }
        }

        saveArtOptionVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                binding.btnSave.isVisible = isVisible
            }
        }

        pickArtOptionVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                binding.btnPickArt.isVisible = isVisible
            }
        }

        deleteArtOptionVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                binding.btnDeleteArt.isVisible = isVisible
            }
        }

        artDeletionConfirmationVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                if (isVisible) {
                    Anim.fadeIn(binding.includeAlbumArtDeletionConfirmation.root)
                } else {
                    Anim.fadeOut(binding.includeAlbumArtDeletionConfirmation.root)
                }
            }
        }

        isSavingChanges.observeNonNull(owner) { isSaving ->
            dialog?.apply {
                if (isSaving) {
                    Anim.fadeIn(binding.includeProgressOverlay.root)
                } else {
                    Anim.fadeOut(binding.includeProgressOverlay.root)
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
                        .into(binding.imvAlbumArt)
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