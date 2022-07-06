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
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.model.menu.ContextualMenu
import com.frolo.muse.model.menu.OptionsMenu
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.ui.ScrolledToTop
import com.frolo.muse.ui.base.OnBackPressedHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.RESPermissionBus
import com.frolo.muse.ui.main.confirmDeletion
import com.frolo.muse.ui.main.confirmShortcutCreation
import com.frolo.music.model.Media
import com.frolo.ui.FragmentUtils


abstract class AbsMediaCollectionFragment <E: Media>: BaseFragment(),
        OnBackPressedHandler,
        ScrolledToTop {

    // Options menu
    private var mediaOptionsDialog: MediaOptionsDialog<E>? = null

    // Contextual menu
    private var actionMode: ActionMode? = null
    private var contextualProgressDialog: Dialog? = null

    abstract val viewModel: AbsMediaCollectionViewModel<E>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RESPermissionBus.dispatcher.observe(this) {
            // We only should access the view model if the fragment is attached to an activity
            // to prevent unexpected crashes while providing the view model.
            if (FragmentUtils.isAttachedToActivity(this)) {
                viewModel.onReadPermissionGrantedSomewhere()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observerViewModel(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            viewModel.onBackArrowClicked()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        viewModel.onStop()
        // Not sure if we should finish it here,
        // But we need finish the action mode, if the fragment is not visible.
        actionMode?.finish()
        actionMode = null
        super.onStop()
    }

    override fun handleOnBackPressed(): Boolean {
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
            actionMode = onShowContextualMenu(contextualMenu)?.also { newActionMode ->
                // Initial count of items selected
                newActionMode.title = (selectedItemsCount.value ?: 0).toString()
            }
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
        confirmDeletionEvent.observeNonNull(owner) { confirmation ->
            context?.confirmDeletion(confirmation) { type ->
                checkReadWritePermissionsFor {
                    viewModel.onConfirmedDeletion(confirmation.mediaItem, type)
                }
            }
        }

        confirmMultipleDeletionEvent.observeNonNull(owner) { confirmation ->
            context?.confirmDeletion(confirmation) { type ->
                checkReadWritePermissionsFor {
                    viewModel.onConfirmedMultipleDeletion(confirmation.mediaItems, type)
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
                MediaOptionsDialog.Option.REMOVE_FROM_QUEUE -> viewModel.onRemoveFromCurrentQueueOptionSelected()
                MediaOptionsDialog.Option.EDIT -> viewModel.onEditOptionSelected()
                MediaOptionsDialog.Option.ADD_TO_PLAYLIST -> viewModel.onAddToPlaylistOptionSelected()
                MediaOptionsDialog.Option.VIEW_LYRICS -> viewModel.onViewLyricsOptionSelected()
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