package com.frolo.muse.ui.main.player.lyrics

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.lyrics.Lyrics
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
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
            setContentView(R.layout.dialog_lyrics)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            setupDialogSize(this, 6 * width / 7, 3 * height / 4)

            imv_close.setOnClickListener { dismiss() }

            btn_save.setOnClickListener {
                val text = tv_lyrics.text?.toString() ?: ""
                viewModel.onSaveButtonClicked(text)
            }


        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            error.observeNonNull(owner) { err ->
                onDisplayError(err)
            }

            isEditable.observeNonNull(owner) { isEditable ->
                onSetEditable(isEditable)
            }

            isLoadingLyrics.observeNonNull(owner) { isLoading ->
                onSetLoadingLyrics(isLoading)
            }

            isSavingLyrics.observeNonNull(owner) { isSaving ->
                onSetSavingLyrics(isSaving)
            }

            lyrics.observeNonNull(owner) { lyrics ->
                onDisplayLyrics(lyrics)
            }

            lyricsSavedEvent.observe(owner) {
                onLyricsSaved()
            }

            songName.observeNonNull(owner) { songName ->
                onDisplaySongName(songName)
            }
        }
    }

    private fun onDisplayError(err: Throwable) {
        dialog?.apply {
            tv_error.text = err.message
        }
    }

    private fun onSetEditable(isEditable: Boolean) {
        dialog?.apply {
            tv_lyrics.isEnabled = isEditable
            if (isEditable) {
                tv_lyrics.inputType =
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                                InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE

                tv_lyrics.isSingleLine = false
                tv_lyrics.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            } else {
                tv_lyrics.inputType = InputType.TYPE_NULL
            }
        }
    }

    private fun onSetLoadingLyrics(isLoading: Boolean) {
        dialog?.apply {
            pb_loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun onSetSavingLyrics(isSaving: Boolean) {
        dialog?.apply {
        }
    }

    private fun onDisplayLyrics(lyrics: Lyrics) {
        dialog?.apply {
            tv_lyrics.setText(lyrics.text)
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