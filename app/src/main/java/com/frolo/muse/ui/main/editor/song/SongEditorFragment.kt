package com.frolo.muse.ui.main.editor.song

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.frolo.muse.GlideManager
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.views.Anim
import com.frolo.muse.views.getNonNullText
import kotlinx.android.synthetic.main.dialog_song_editor.*


class SongEditorFragment: BaseDialogFragment() {

    companion object {
        private const val ARG_SONG = "song"

        // Factory
        fun newInstance(item: Song) = SongEditorFragment()
                .withArg(ARG_SONG, item)
    }

    private val viewModel: SongEditorViewModel by lazy {
        val app = requireApp()
        val vmFactory = SongEditorVMFactory(app, app.appComponent, song)
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
            setContentView(R.layout.dialog_song_editor)
            initUI(this)

            // set custom dialog size
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            setupDialogSize(this, 11 * width / 12, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun initUI(dialog: Dialog) {
        dialog.apply {
            btn_cancel.setOnClickListener { dismiss() }

            btn_save.setOnClickListener {
                checkWritePermissionFor {
                    val title = edt_song_name.getNonNullText()
                    val album = edt_album_name.getNonNullText()
                    val artist = edt_artist_name.getNonNullText()
                    val genre = edt_genre_name.getNonNullText()
                    viewModel.onSaveClicked(title, album, artist, genre)
                }
            }

            edt_song_name.setText(song.title)
            edt_album_name.setText(song.album)
            edt_artist_name.setText(song.artist)
            edt_genre_name.setText(song.genre)

            tv_duration.text = getString(R.string.duration, song.getDurationString())
            tv_filepath.text = song.source

            val options = GlideManager.get().requestOptions(song.albumId)
            Glide.with(this@SongEditorFragment)
                    .load(GlideManager.albumArtUri(song.albumId))
                    .apply(options)
                    .into(imv_album_art)
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            isLoadingUpdate.observeNonNull(owner) { isLoading ->
                onSetLoading(isLoading)
            }

            updateError.observeNonNull(owner) { err ->
                onDisplayError(err)
            }

            updatedSong.observeNonNull(owner) { newSong ->
                onSongUpdated(newSong)
            }
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        dialog?.apply {
            if (isLoading) {
                Anim.fadeIn(inc_progress_overlay)
            } else {
                Anim.fadeOut(inc_progress_overlay)
            }
        }
    }

    private fun onDisplayError(err: Throwable) {
        postError(err)
        dismiss()
    }

    private fun onSongUpdated(newSong: Song) {
        dismiss()
    }
}