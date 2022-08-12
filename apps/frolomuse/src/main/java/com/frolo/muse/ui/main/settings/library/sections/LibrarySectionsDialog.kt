package com.frolo.muse.ui.main.settings.library.sections

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logLibrarySectionsSaved
import com.frolo.muse.model.Library
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.dialog_library_sections.*


class LibrarySectionsDialog : BaseDialogFragment(),
    LibrarySectionAdapter.OnDragListener {

    private val preferences: Preferences by prefs()

    private val eventLogger: EventLogger by eventLogger()

    /**
     * The original sections of the library, currently saved in the preferences.
     */
    private val originalSections: List<@Library.Section Int>
        get() {
            return preferences.librarySections
        }

    /**
     * The original enabled status of the library sections, currently saved in the preferences.
     */
    private val originalEnabledStatus: Map<@Library.Section Int, Boolean>
        get() {
            return originalSections.associateBy({ it }, { preferences.isLibrarySectionEnabled(it) })
        }

    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_library_sections)
            setupDialogSizeByDefault(this)
            loadUI(this)
        }
    }

    override fun onTouchDragView(holder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(holder)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        saveChanges()
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {

        val adapter = LibrarySectionAdapter(
            onDragListener = this@LibrarySectionsDialog,
            sections = originalSections,
            enabledStatus = originalEnabledStatus
        )

        rv_sections.layoutManager = LinearLayoutManager(context)
        rv_sections.adapter = adapter
        val callback = SimpleItemTouchHelperCallback(adapter = adapter, itemViewSwipeEnabled = false)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rv_sections)
        itemTouchHelper = touchHelper

        btn_save.setOnClickListener {
            dismiss()
        }
    }

    // The best place to save the changes
    override fun onStop() {
        saveChanges()
        super.onStop()
    }

    private fun saveChanges() {
        val adapter = dialog?.rv_sections?.adapter as? LibrarySectionAdapter
        if (adapter != null) {

            val newSections = adapter.getSnapshot()
            val newEnabledStatus = adapter.getEnabledStatusSnapshot()

            // Need to check if it actually has been changed

            val sectionsChanged = run {
                newSections != originalSections
            }

            val enabledStatusChanged = run {
                newEnabledStatus != originalEnabledStatus
            }

            if (sectionsChanged || enabledStatusChanged) {
                preferences.librarySections = newSections
                for (entry in newEnabledStatus.entries) {
                    preferences.setLibrarySectionEnabled(entry.key, entry.value)
                }

                eventLogger.logLibrarySectionsSaved(changed = true)
            }
        }
    }

    companion object {

        // Factory
        fun newInstance() = LibrarySectionsDialog()

    }

}