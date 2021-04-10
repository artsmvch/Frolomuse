package com.frolo.muse.ui.main.editor.song

import android.app.Activity
import android.app.Dialog
import android.app.RecoverableSecurityException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.views.Anim
import com.frolo.muse.views.getNonNullText
import kotlinx.android.synthetic.main.dialog_song_editor.*


class SongEditorDialog: BaseDialogFragment() {

    private val viewModel: SongEditorViewModel by lazy {
        val app = requireFrolomuseApp()
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
            loadUI(this)

            // set custom dialog size
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            setupDialogSize(this, 11 * width / 12, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        btn_cancel.setOnClickListener {
            dismiss()
        }

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

        tv_filepath.text = song.source

        Glide.with(this@SongEditorDialog)
            .makeRequest(song.albumId)
            .circleCrop()
            .placeholder(R.drawable.ic_framed_music_note)
            .error(R.drawable.ic_framed_music_note)
            .into(imv_album_art)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
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
                Anim.fadeIn(inc_progress_overlay)
            } else {
                Anim.fadeOut(inc_progress_overlay)
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
                dialog?.apply {
                    viewModel.onUserGrantedWritePermission(
                        title = edt_song_name.getNonNullText(),
                        album = edt_album_name.getNonNullText(),
                        artist = edt_artist_name.getNonNullText(),
                        genre = edt_genre_name.getNonNullText()
                    )
                }
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