package com.frolo.muse.ui.main.editor.song

import android.app.Activity
import android.app.Dialog
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.view.Window
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.core.ui.glide.makeAlbumArtRequest
import com.frolo.muse.R
import com.frolo.muse.databinding.DialogSongEditorBinding
import com.frolo.muse.di.activityComponent
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.settings.updateText
import com.frolo.muse.views.Anim
import com.frolo.music.model.Song


class SongEditorDialog: BaseDialogFragment() {
    private var _binding: DialogSongEditorBinding? = null
    private val binding: DialogSongEditorBinding get() = _binding!!

    private val viewModel: SongEditorViewModel by lazy {
        val vmFactory = SongEditorVMFactory(activityComponent, song)
        ViewModelProviders.of(this, vmFactory)
            .get(SongEditorViewModel::class.java)
    }

    private val song: Song by serializableArg(ARG_SONG)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)

            _binding = DialogSongEditorBinding.inflate(layoutInflater)
            setContentView(binding.root)
            loadUI(this)

            // set custom dialog size
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            setupDialogSize(this, 11 * width / 12, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            viewModel.onSaveClicked()
        }

        binding.edtSongName.doAfterTextChanged {
            viewModel.onTitleChanged(it?.toString())
        }

        binding.edtAlbumName.doAfterTextChanged {
            viewModel.onAlbumChanged(it?.toString())
        }

        binding.edtArtistName.doAfterTextChanged {
            viewModel.onArtistChanged(it?.toString())
        }

        binding.edtGenreName.doAfterTextChanged {
            viewModel.onGenreChanged(it?.toString())
        }

        binding.tvFilepath.text = song.source

        Glide.with(this@SongEditorDialog)
            .makeAlbumArtRequest(song.albumId)
            .circleCrop()
            .placeholder(R.drawable.ic_framed_music_note)
            .error(R.drawable.ic_framed_music_note)
            .into(binding.imvAlbumArt)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        title.observe(owner) { text ->
            dialog?.apply {
                binding.edtSongName.updateText(text)
            }
        }

        album.observe(owner) { text ->
            dialog?.apply {
                binding.edtAlbumName.updateText(text)
            }
        }

        artist.observe(owner) { text ->
            dialog?.apply {
                binding.edtArtistName.updateText(text)
            }
        }

        genre.observe(owner) { text ->
            dialog?.apply {
                binding.edtGenreName.updateText(text)
            }
        }

        handleWriteRequestEvent.observeNonNull(owner) { song ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val contentResolver = requireContext().contentResolver
                val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.let { ContentUris.withAppendedId(it, song.id) }
                val pendingIntent = MediaStore.createWriteRequest(contentResolver, listOf(songUri))
                startIntentSenderForResult(pendingIntent.intentSender,
                        RC_REQUEST_WRITE_PERMISSION, null, 0, 0, 0, null)
            } else {
                checkWritePermissionFor {
                    viewModel.onUserHandledWriteRequest()
                }
            }
        }

        isLoadingUpdate.observeNonNull(owner) { isLoading ->
            onSetLoading(isLoading)
        }

        updateError.observeNonNull(owner) { err ->
            onHandleError(err)
        }

        updatedSong.observeNonNull(owner) { newSong ->
            onSongUpdated(newSong)
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        dialog?.apply {
            if (isLoading) {
                Anim.fadeIn(binding.incProgressOverlay.root)
            } else {
                Anim.fadeOut(binding.incProgressOverlay.root)
            }
        }
    }

    private fun onHandleError(err: Throwable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && err is RecoverableSecurityException) {
            val intentSender = err.userAction.actionIntent.intentSender
            if (intentSender != null) {
                // Let's try asking the user to grant us the write permission in order to save the changes
                startIntentSenderForResult(intentSender, RC_REQUEST_WRITE_PERMISSION,
                        null, 0, 0, 0, null)
                return
            }
        }

        postError(err)
        dismiss()
    }

    private fun onSongUpdated(newSong: Song) {
        dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_REQUEST_WRITE_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.onUserHandledWriteRequest()
            } else {
                dismiss()
            }
        }
    }

    companion object {
        private const val ARG_SONG = "song"

        private const val RC_REQUEST_WRITE_PERMISSION = 8001

        // Factory
        fun newInstance(item: Song) = SongEditorDialog()
                .withArg(ARG_SONG, item)
    }

}