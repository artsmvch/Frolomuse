package com.frolo.muse.ui.main.library.base

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.frolo.muse.R
import com.frolo.muse.databinding.DialogMediaOptionsBinding
import com.frolo.ui.StyleUtils
import com.frolo.music.model.Media
import com.frolo.muse.model.menu.OptionsMenu
import com.frolo.muse.ui.Dialogs
import com.frolo.muse.ui.getName
import com.frolo.muse.ui.getTypeName
import com.frolo.muse.views.Anim
import com.google.android.material.bottomsheet.BottomSheetDialog


class MediaOptionsDialog<E: Media> constructor(
    context: Context,
    private val optionsMenu: OptionsMenu<E>,
    private val onOptionSelected: (item: E, option: Option) -> Unit
): BottomSheetDialog(context) {

    enum class Option {
        SHARE, DELETE, LIKE, PLAY, PLAY_NEXT, ADD_TO_QUEUE, REMOVE_FROM_QUEUE,
        EDIT, ADD_TO_PLAYLIST, VIEW_LYRICS, VIEW_ALBUM, VIEW_ARTIST,
        VIEW_GENRE, SET_AS_DEFAULT, HIDE, SCAN_FILES, CREATE_SHORTCUT
    }

    private var _binding: DialogMediaOptionsBinding? = null
    private val binding: DialogMediaOptionsBinding get() = _binding!!

    private val iconTint = StyleUtils.resolveColor(context, R.attr.iconTintMuted)
    private val drawableHeart: Drawable
    private val drawableFilledHeart: Drawable

    init {
        drawableHeart = ContextCompat.getDrawable(context, R.drawable.ic_heart)!!.apply {
            mutate().setColorFilter(iconTint, PorterDuff.Mode.SRC_ATOP)
        }
        drawableFilledHeart = ContextCompat.getDrawable(context, R.drawable.ic_filled_heart)!!
        Dialogs.fixBottomSheet(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DialogMediaOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item: E = optionsMenu.item
        with(binding) {
            btnSetAsDefault.setOnClickListener { onOptionSelected(item, Option.SET_AS_DEFAULT) }
            btnAddToHidden.setOnClickListener { onOptionSelected(item, Option.HIDE) }
            btnScanFiles.setOnClickListener { onOptionSelected(item, Option.SCAN_FILES) }
            btnPlay.setOnClickListener { onOptionSelected(item, Option.PLAY) }
            btnShare.setOnClickListener { onOptionSelected(item, Option.SHARE) }
            btnDelete.setOnClickListener { onOptionSelected(item, Option.DELETE) }
            btnAddToPlaylist.setOnClickListener { onOptionSelected(item, Option.ADD_TO_PLAYLIST) }
            btnAddToQueue.setOnClickListener { onOptionSelected(item, Option.ADD_TO_QUEUE) }
            btnPlayNext.setOnClickListener { onOptionSelected(item, Option.PLAY_NEXT) }
            btnRemoveFromQueue.setOnClickListener { onOptionSelected(item, Option.REMOVE_FROM_QUEUE) }
            btnViewLyrics.setOnClickListener { onOptionSelected(item, Option.VIEW_LYRICS) }
            btnViewAlbum.setOnClickListener { onOptionSelected(item, Option.VIEW_ALBUM) }
            btnViewArtist.setOnClickListener { onOptionSelected(item, Option.VIEW_ARTIST) }
            btnEdit.setOnClickListener { onOptionSelected(item, Option.EDIT) }
            btnLike.setOnClickListener { onOptionSelected(item, Option.LIKE) }
            btnCreateShortcut.setOnClickListener { onOptionSelected(item, Option.CREATE_SHORTCUT) }

            with(btnLike) {
                visibility = if (optionsMenu.favouriteOptionAvailable) View.VISIBLE else View.GONE
                setImageDrawable(if (optionsMenu.isFavourite) drawableFilledHeart else drawableHeart)
            }

            btnViewLyrics.visibility = if (optionsMenu.viewLyricsOptionAvailable) View.VISIBLE else View.GONE
            btnViewAlbum.visibility = if (optionsMenu.viewAlbumOptionAvailable) View.VISIBLE else View.GONE
            btnViewArtist.visibility = if (optionsMenu.viewArtistOptionAvailable) View.VISIBLE else View.GONE
            btnEdit.visibility = if (optionsMenu.editOptionAvailable) View.VISIBLE else View.GONE
            btnAddToPlaylist.visibility = if (optionsMenu.addToPlaylistOptionAvailable) View.VISIBLE else View.GONE
            btnRemoveFromQueue.visibility = if (optionsMenu.removeFromQueueOptionAvailable) View.VISIBLE else View.GONE
            btnSetAsDefault.visibility = if (optionsMenu.setAsDefaultOptionAvailable) View.VISIBLE else View.GONE
            btnAddToHidden.visibility = if (optionsMenu.addToHiddenOptionAvailable) View.VISIBLE else View.GONE
            btnScanFiles.visibility = if (optionsMenu.scanFilesOptionAvailable) View.VISIBLE else View.GONE
            btnCreateShortcut.visibility = if (optionsMenu.shortcutOptionAvailable) View.VISIBLE else View.GONE

            tvMediaName.text = item.getName()
            tvMediaType.text = item.getTypeName(context.resources)
        }
    }

    fun setLiked(favourite: Boolean) {
        with(binding.btnLike) {
            setImageDrawable(if (favourite) drawableFilledHeart else drawableHeart)
            if (favourite) Anim.like(this) else Anim.unlike(this)
        }
    }

}