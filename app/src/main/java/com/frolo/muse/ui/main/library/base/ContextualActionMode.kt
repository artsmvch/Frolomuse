package com.frolo.muse.ui.main.library.base

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.frolo.muse.R
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.menu.ContextualMenu


class ContextualActionMode<E: Media> constructor(
        private val contextualMenu: ContextualMenu<E>,
        private val onOptionSelected: (option: Option) -> Unit
): ActionMode.Callback {

    enum class Option {
        CLOSE,
        SELECT_ALL,
        PLAY,
        PLAY_NEXT,
        ADD_TO_QUEUE,
        SHARE,
        DELETE,
        ADD_TO_PLAYLIST,
        HIDE
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val option = when(item.itemId) {
            R.id.action_select_all -> Option.SELECT_ALL
            R.id.action_hide -> Option.HIDE
            R.id.action_play -> Option.PLAY
            R.id.action_play_next -> Option.PLAY_NEXT
            R.id.action_add_to_queue -> Option.ADD_TO_QUEUE
            R.id.action_share -> Option.SHARE
            R.id.action_delete -> Option.DELETE
            R.id.action_add_to_playlist -> Option.ADD_TO_PLAYLIST
            else -> throw IllegalArgumentException("Unknown menu item id: ${item.itemId}")
        }
        onOptionSelected(option)
        return true
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.fragment_base_list_context, menu)
        // configuring menu
        menu.findItem(R.id.action_select_all)?.apply { isVisible = contextualMenu.selectAllOptionAvailable }
        menu.findItem(R.id.action_hide)?.apply { isVisible = contextualMenu.hideOptionAvailable }
        menu.findItem(R.id.action_play)?.apply { isVisible = contextualMenu.playOptionAvailable }
        menu.findItem(R.id.action_play_next)?.apply { isVisible = contextualMenu.playNextOptionAvailable }
        menu.findItem(R.id.action_add_to_queue)?.apply { isVisible = contextualMenu.addToQueueOptionAvailable }
        menu.findItem(R.id.action_share)?.apply { isVisible = contextualMenu.shareOptionAvailable }
        menu.findItem(R.id.action_delete)?.apply { isVisible = contextualMenu.deleteOptionAvailable }
        menu.findItem(R.id.action_add_to_playlist)?.apply { isVisible = contextualMenu.addToPlaylistOptionAvailable }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        onOptionSelected(Option.CLOSE)
    }
}