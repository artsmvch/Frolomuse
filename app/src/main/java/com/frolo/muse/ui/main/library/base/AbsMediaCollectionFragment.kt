package com.frolo.muse.ui.main.library.base

import android.app.Dialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.*
import com.frolo.muse.model.menu.ContextualMenu
import com.frolo.muse.model.menu.OptionsMenu
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.ui.base.BackPressHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.RESPermissionObserver
import com.frolo.muse.ui.getDeleteConfirmationMessage
import com.frolo.muse.ui.main.confirmDeletion
import com.frolo.muse.ui.main.confirmShortcutCreation


abstract class AbsMediaCollectionFragment <E: Media>: BaseFragment(),
        BackPressHandler {

    // Options menu
    private var mediaOptionsDialog: MediaOptionsDialog<E>? = null

    // Contextual menu
    private var actionMode: ActionMode? = null
    private var contextualProgressDialog: Dialog? = null

    abstract val viewModel: AbsMediaCollectionViewModel<E>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.also { safeContext ->
            RESPermissionObserver.observe(safeContext, this) {
                viewModel.onReadPermissionGrantedSomewhere()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.onActive()
        observerViewModel(viewLifecycleOwner)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            viewModel.onBackArrowClicked()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        // Not sure if we should finish it here,
        // But we need finish the action mode, if the fragment is not visible.
        actionMode?.finish()
        actionMode = null
        super.onStop()
    }

    override fun onBackPress(): Boolean {
        viewModel.onBackPressed()
        return true
    }

    // Creates a popup menu according to the given sort order menu.
    // Inheritors may need override it,
    // because default implementation tries to show the popup at the view with [R.id.action_sort] id.
    // If you don't have such a view or want to show it at other location (i.e. other anchor view),
    // then the overriding is necessary.
    protected open fun onShowSortOrderMenu(sortOrderMenu: SortOrderMenu) {
        // First trying to find the corresponding view in the fragment view
        val anchorViewInFragment: View? = view?.findViewById(R.id.action_sort)
        // If null, then trying to find the view in the host activity
        val anchorView: View? = anchorViewInFragment?: activity?.findViewById(R.id.action_sort)

        anchorView?.let { safeAnchorView ->
            safeAnchorView.showSortOrderPopup(
                sortOrderMenu = sortOrderMenu,
                sortOrderConsumer = { sortOrder -> viewModel.onSortOrderSelected(sortOrder) },
                reversedConsumer = { reversed -> viewModel.onSortOrderReversedChanged(reversed) }
            )
        }
    }

    private fun observerViewModel(owner: LifecycleOwner) = with(viewModel) {
        // Permissions
        // Unused for now
//        askReadPermissionEvent.observe(owner) {
//            checkReadPermissionFor {
//                viewModel.onReadPermissionGranted()
//            }
//        }

        // Error
        error.observeNonNull(owner) { err ->
            onDisplayError(err)
        }

        // Common
        deletedItemsEvent.observeNonNull(owner) {
            toastShortMessage(R.string.deleted)
        }

        // sort order
        openSortOrderMenuEvent.observeNonNull(owner) { sortOrderMenu: SortOrderMenu ->
            onShowSortOrderMenu(sortOrderMenu)
        }

        // Media collection
        mediaList.observeNonNull(owner) { list ->
            onSubmitList(list)
        }

        isLoading.observeNonNull(owner) { isLoading ->
            onSetLoading(isLoading)
        }

        placeholderVisible.observeNonNull(owner) { isVisible ->
            onSetPlaceholderVisible(isVisible)
        }

        // Options menu
        openOptionsMenuEvent.observeNonNull(owner) { optionsMenu ->
            mediaOptionsDialog?.cancel()
            mediaOptionsDialog = onShowOptionsMenuDialog(optionsMenu)
        }

        closeOptionsMenuEvent.observeNonNull(owner) { optionsMenu ->
            mediaOptionsDialog?.cancel()
            mediaOptionsDialog = null
        }

        optionsMenuItemFavourite.observeNonNull(owner) { isFavourite ->
            mediaOptionsDialog?.setLiked(isFavourite)
        }

        // Contextual menu
        openContextualMenuEvent.observeNonNull(owner) { contextualMenu ->
            actionMode?.finish()
            actionMode = onShowContextualMenu(contextualMenu)
        }

        selectedItems.observeNonNull(owner) { selectedItems ->
            onSubmitSelectedItems(selectedItems)
        }

        selectedItemsCount.observeNonNull(owner) { count ->
            actionMode?.title = count.toString()
        }

        isInContextualMode.observeNonNull(owner) { isInContextualMode ->
            if (!isInContextualMode) {
                actionMode?.finish()
                actionMode = null
            }
        }

        isProcessingContextual.observeNonNull(owner) { isProcessingContextual ->
            if (isProcessingContextual) {
                contextualProgressDialog?.cancel()
                contextualProgressDialog = onShowContextualProgressDialog()
            } else {
                contextualProgressDialog?.cancel()
            }
        }

        // Deletion confirmation
        confirmDeletionEvent.observeNonNull(owner) { item ->
            context?.also { safeContext ->
                val msg = safeContext.getDeleteConfirmationMessage(item)

                safeContext.confirmDeletion(msg) {
                    checkReadWritePermissionsFor { viewModel.onConfirmedDeletion(item) }
                }
            }
        }

        confirmMultipleDeletionEvent.observeNonNull(owner) { items ->
            context?.also { safeContext ->
                val msg = safeContext.getDeleteConfirmationMessage(items)

                safeContext.confirmDeletion(msg) {
                    checkReadWritePermissionsFor { viewModel.onConfirmedMultipleDeletion(items) }
                }
            }
        }

        // Events
        addedNextToQueue.observeNonNull(owner) {
            toastShortMessage(R.string.will_be_played_next)
        }

        addedToQueue.observeNonNull(owner) {
            toastShortMessage(R.string.added_to_queue)
        }

        confirmShortcutCreationEvent.observeNonNull(owner) { item ->
            context?.confirmShortcutCreation(item) {
                viewModel.onCreateShortcutOptionConfirmed(item)
            }
        }

        shortcutCreatedEvent.observeNonNull(owner) {
            toastShortMessage(R.string.shortcut_created)
        }
    }

    private fun onShowOptionsMenuDialog(optionsMenu: OptionsMenu<E>): MediaOptionsDialog<E> {
        val dialog = MediaOptionsDialog(requireContext(), optionsMenu) { _, option ->
            when (option) {
                MediaOptionsDialog.Option.SET_AS_DEFAULT -> viewModel.onSetAsDefaultOptionSelected()
                MediaOptionsDialog.Option.HIDE -> viewModel.onHideOptionSelected()
                MediaOptionsDialog.Option.SCAN_FILES -> viewModel.onScanFilesOptionSelected()
                MediaOptionsDialog.Option.SHARE -> viewModel.onShareOptionSelected()
                MediaOptionsDialog.Option.DELETE -> viewModel.onDeleteOptionSelected()
                MediaOptionsDialog.Option.LIKE -> viewModel.onLikeOptionClicked()
                MediaOptionsDialog.Option.PLAY -> viewModel.onPlayOptionSelected()
                MediaOptionsDialog.Option.PLAY_NEXT -> viewModel.onPlayNextOptionSelected()
                MediaOptionsDialog.Option.ADD_TO_QUEUE -> viewModel.onAddToQueueOptionSelected()
                MediaOptionsDialog.Option.EDIT -> viewModel.onEditOptionSelected()
                MediaOptionsDialog.Option.ADD_TO_PLAYLIST -> viewModel.onAddToPlaylistOptionSelected()
                MediaOptionsDialog.Option.VIEW_ALBUM -> viewModel.onViewAlbumOptionSelected()
                MediaOptionsDialog.Option.VIEW_ARTIST -> viewModel.onViewArtistOptionSelected()
                MediaOptionsDialog.Option.VIEW_GENRE -> viewModel.onViewGenreOptionSelected()
                MediaOptionsDialog.Option.CREATE_SHORTCUT -> viewModel.onCreateShortcutOptionSelected()
            }
        }
        return dialog.apply { show() }
    }

    private fun onShowContextualMenu(contextualMenu: ContextualMenu<E>): ActionMode? {
        return (activity as? AppCompatActivity)?.run {
            val actionModeCallback = ContextualActionMode(contextualMenu) { option ->
                when (option) {
                    ContextualActionMode.Option.SELECT_ALL -> viewModel.onSelectAllContextualOptionSelected()
                    ContextualActionMode.Option.SCAN_FILES -> viewModel.onScanFilesContextualOptionSelected()
                    ContextualActionMode.Option.HIDE -> viewModel.onHideContextualOptionSelected()
                    ContextualActionMode.Option.PLAY -> viewModel.onPlayContextualOptionSelected()
                    ContextualActionMode.Option.PLAY_NEXT -> viewModel.onPlayNextContextualOptionSelected()
                    ContextualActionMode.Option.ADD_TO_QUEUE -> viewModel.onAddToQueueContextualOptionSelected()
                    ContextualActionMode.Option.SHARE -> viewModel.onShareContextualOptionSelected()
                    ContextualActionMode.Option.DELETE -> viewModel.onDeleteContextualOptionSelected()
                    ContextualActionMode.Option.ADD_TO_PLAYLIST -> viewModel.onAddToPlaylistContextualOptionSelected()
                    ContextualActionMode.Option.CLOSE -> viewModel.onContextualMenuClosed()
                }
            }
            startSupportActionMode(actionModeCallback)
        }
    }

    private fun onShowContextualProgressDialog(): Dialog {
        return Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_progress)
            findViewById<TextView>(R.id.tv_message).setText(R.string.loading)
            setOnDismissListener {
                viewModel.onContextualDialogClosed()
            }
            setOnCancelListener {
                viewModel.onContextualDialogClosed()
            }
            show()
        }
    }

    abstract fun onSetLoading(loading: Boolean)
    abstract fun onSubmitList(list: List<E>)
    abstract fun onSubmitSelectedItems(selectedItems: Set<E>)
    abstract fun onSetPlaceholderVisible(visible: Boolean)
    abstract fun onDisplayError(err: Throwable)
}