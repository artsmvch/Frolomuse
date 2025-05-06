package com.frolo.muse.ui.main.player.lyrics

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.BuildConfig
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.DialogLyricsBinding
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.Song
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.settings.updateText


class LyricsDialogFragment: BaseDialogFragment() {

    companion object {
        private const val ARG_SONG = "song"

        // Factory
        fun newInstance(song: Song) = LyricsDialogFragment().withArg(ARG_SONG, song)
    }

    private var _binding: DialogLyricsBinding? = null
    private val binding: DialogLyricsBinding get() = _binding!!

    private val viewModel: LyricsViewModel by lazy {
        val song = requireArguments().getSerializable(ARG_SONG) as Song
        val vmFactory = LyricsVMFactory(activityComponent, song)
        ViewModelProviders.of(this, vmFactory)
            .get(LyricsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

            _binding = DialogLyricsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupDialogSizeRelativelyToScreen(this, widthPercent = 19f / 20f)

            binding.imvClose.setOnClickListener {
                dismiss()
            }

            binding.edtLyrics.doOnTextChanged { text, _, _, _ ->
                viewModel.onLyricsEdited(text?.toString())
            }
            binding.edtLyrics.clearFocus()

            binding.btnSave.setOnClickListener {
                viewModel.onSaveButtonClicked()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            onDisplayError(err)
        }

        isEditable.observeNonNull(owner) { isEditable ->
            onSetEditable(isEditable)
        }

        isLoadingLyrics.observeNonNull(owner) { isLoading ->
            onSetLoadingLyrics(isLoading)
        }

        isLyricsVisible.observeNonNull(owner) { isVisible ->
            onSetLyricsVisibility(isVisible)
        }

        isSavingLyrics.observeNonNull(owner) { isSaving ->
            onSetSavingLyrics(isSaving)
        }

        lyricsText.observeNonNull(owner) { text ->
            onDisplayLyricsText(text)
        }

        lyricsSavedEvent.observe(owner) {
            onLyricsSaved()
        }

        songName.observeNonNull(owner) { songName ->
            onDisplaySongName(songName)
        }
    }

    private fun onDisplayError(err: Throwable) {
        if (BuildConfig.DEBUG) {
            postError(err)
        }
    }

    private fun onSetEditable(isEditable: Boolean) {
        dialog?.apply {
            binding.edtLyrics.isEnabled = isEditable
            if (isEditable) {
                binding.edtLyrics.inputType =
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                                InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE

                binding.edtLyrics.isSingleLine = false
                binding.edtLyrics.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            } else {
                binding.edtLyrics.inputType = InputType.TYPE_NULL
            }
        }
    }

    private fun onSetLoadingLyrics(isLoading: Boolean) {
        dialog?.apply {
            binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun onSetLyricsVisibility(isVisible: Boolean) {
        dialog?.apply {
            binding.edtLyrics.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
            binding.edtLyrics.clearFocus()
        }
    }

    private fun onSetSavingLyrics(isSaving: Boolean) {
        dialog?.apply {
        }
    }

    private fun onDisplayLyricsText(lyricsText: String) {
        dialog?.apply {
            binding.edtLyrics.updateText(lyricsText)
        }
    }

    private fun onDisplaySongName(songName: String) {
        dialog?.apply {
            binding.tvSongName.text = songName
        }
    }

    private fun onLyricsSaved() {
        // dismiss or not?
        dismiss()
    }
}