package com.frolo.muse.ui.main.library.base

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.menu.OptionsMenu
import com.frolo.muse.ui.Dialogs
import com.frolo.muse.ui.getName
import com.frolo.muse.ui.getTypeName
import com.frolo.muse.views.Anim
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_media_options.*


class MediaOptionsDialog<E: Media> constructor(
    context: Context,
    private val optionsMenu: OptionsMenu<E>,
    private val onOptionSelected: (item: E, option: Option) -> Unit
): BottomSheetDialog(context) {

    enum class Option {
        SHARE, DELETE, LIKE, PLAY, PLAY_NEXT, ADD_TO_QUEUE, REMOVE_FROM_QUEUE,
        EDIT, ADD_TO_PLAYLIST, VIEW_ALBUM, VIEW_ARTIST,
        VIEW_GENRE, SET_AS_DEFAULT, HIDE, SCAN_FILES, CREATE_SHORTCUT
    }

    private val iconTint = StyleUtil.readColorAttrValue(context, R.attr.iconImageTint)
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
        setupOptionsMenu(optionsMenu)
    }

    private fun setupOptionsMenu(optionsMenu: OptionsMenu<E>) {
        val item: E = optionsMenu.item

        setContentView(R.layout.dialog_media_options)

        with(this) {
            btn_set_as_default.setOnClickListener { onOptionSelected(item, Option.SET_AS_DEFAULT) }
            btn_add_to_hidden.setOnClickListener { onOptionSelected(item, Option.HIDE) }
            btn_scan_files.setOnClickListener { onOptionSelected(item, Option.SCAN_FILES) }
            btn_play.setOnClickListener { onOptionSelected(item, Option.PLAY) }
            btn_share.setOnClickListener { onOptionSelected(item, Option.SHARE) }
            btn_delete.setOnClickListener { onOptionSelected(item, Option.DELETE) }
            btn_add_to_playlist.setOnClickListener { onOptionSelected(item, Option.ADD_TO_PLAYLIST) }
            btn_add_to_queue.setOnClickListener { onOptionSelected(item, Option.ADD_TO_QUEUE) }
            btn_play_next.setOnClickListener { onOptionSelected(item, Option.PLAY_NEXT) }
            btn_remove_from_queue.setOnClickListener { onOptionSelected(item, Option.REMOVE_FROM_QUEUE) }
            btn_view_album.setOnClickListener { onOptionSelected(item, Option.VIEW_ALBUM) }
            btn_view_artist.setOnClickListener { onOptionSelected(item, Option.VIEW_ARTIST) }
            btn_edit.setOnClickListener { onOptionSelected(item, Option.EDIT) }
            btn_like.setOnClickListener { onOptionSelected(item, Option.LIKE) }
            btn_create_shortcut.setOnClickListener { onOptionSelected(item, Option.CREATE_SHORTCUT) }

            with(btn_like) {
                visibility = if (optionsMenu.favouriteOptionAvailable) View.VISIBLE else View.GONE
                setImageDrawable(if (optionsMenu.isFavourite) drawableFilledHeart else drawableHeart)
            }

            btn_view_album.visibility = if (optionsMenu.viewAlbumOptionAvailable) View.VISIBLE else View.GONE
            btn_view_artist.visibility = if (optionsMenu.viewArtistOptionAvailable) View.VISIBLE else View.GONE
            btn_edit.visibility = if (optionsMenu.editOptionAvailable) View.VISIBLE else View.GONE
            btn_add_to_playlist.visibility = if (optionsMenu.addToPlaylistOptionAvailable) View.VISIBLE else View.GONE
            btn_remove_from_queue.visibility = if (optionsMenu.removeFromQueueOptionAvailable) View.VISIBLE else View.GONE
            btn_set_as_default.visibility = if (optionsMenu.setAsDefaultOptionAvailable) View.VISIBLE else View.GONE
            btn_add_to_hidden.visibility = if (optionsMenu.addToHiddenOptionAvailable) View.VISIBLE else View.GONE
            btn_scan_files.visibility = if (optionsMenu.scanFilesOptionAvailable) View.VISIBLE else View.GONE
            btn_create_shortcut.visibility = if (optionsMenu.shortcutOptionAvailable) View.VISIBLE else View.GONE

            tv_media_name.text = item.getName()
            tv_media_type.text = item.getTypeName(context.resources)
        }
    }

    fun setLiked(favourite: Boolean) {
        with(btn_like) {
            setImageDrawable(if (favourite) drawableFilledHeart else drawableHeart)
            Anim.like(this)
        }
    }

}