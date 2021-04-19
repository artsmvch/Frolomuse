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
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.settings.updateText
import kotlinx.android.synthetic.main.dialog_lyrics.*


class LyricsDialogFragment: BaseDialogFragment() {

    companion object {
        private const val ARG_SONG = "song"

        // Factory
        fun newInstance(song: Song) = LyricsDialogFragment()
                .withArg(ARG_SONG, song)
    }

    private val viewModel: LyricsViewModel by lazy {
        val song = requireArguments().getSerializable(ARG_SONG) as Song
        val vmFactory = LyricsVMFactory(requireFrolomuseApp().appComponent, song)
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
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            setContentView(R.layout.dialog_lyrics)

            setupDialogSizeRelativelyToScreen(this, widthPercent = 19f / 20f)

            imv_close.setOnClickListener {
                dismiss()
            }

            edt_lyrics.doOnTextChanged { text, _, _, _ ->
                viewModel.onLyricsEdited(text?.toString())
            }
            edt_lyrics.clearFocus()

            btn_save.setOnClickListener {
                viewModel.onSaveButtonClicked()
            }
        }
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
            edt_lyrics.isEnabled = isEditable
            if (isEditable) {
                edt_lyrics.inputType =
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                                InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE

                edt_lyrics.isSingleLine = false
                edt_lyrics.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            } else {
                edt_lyrics.inputType = InputType.TYPE_NULL
            }
        }
    }

    private fun onSetLoadingLyrics(isLoading: Boolean) {
        dialog?.apply {
            pb_loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun onSetLyricsVisibility(isVisible: Boolean) {
        dialog?.apply {
            edt_lyrics.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun onSetSavingLyrics(isSaving: Boolean) {
        dialog?.apply {
        }
    }

    private fun onDisplayLyricsText(lyricsText: String) {
        dialog?.apply {
            edt_lyrics.updateText(lyricsText)
        }
    }

    private fun onDisplaySongName(songName: String) {
        dialog?.apply {
            tv_song_name.text = songName
        }
    }

    private fun onLyricsSaved() {
        // dismiss or not?
        dismiss()
    }
}