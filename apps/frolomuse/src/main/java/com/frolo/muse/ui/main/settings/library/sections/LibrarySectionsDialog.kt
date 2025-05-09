package com.frolo.muse.ui.main.settings.library.sections

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.databinding.DialogLibrarySectionsBinding
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logLibrarySectionsSaved
import com.frolo.muse.model.Library
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback


class LibrarySectionsDialog : BaseDialogFragment(),
    LibrarySectionAdapter.OnDragListener {

    private var _binding: DialogLibrarySectionsBinding? = null
    private val binding: DialogLibrarySectionsBinding get() = _binding!!

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
            _binding = DialogLibrarySectionsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupDialogSizeByDefault(this)
            loadUi(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTouchDragView(holder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(holder)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        saveChanges()
    }

    private fun loadUi(dialog: Dialog) = with(binding) {
        val adapter = LibrarySectionAdapter(
            onDragListener = this@LibrarySectionsDialog,
            sections = originalSections,
            enabledStatus = originalEnabledStatus
        )

        rvSections.layoutManager = LinearLayoutManager(context)
        rvSections.adapter = adapter
        val callback = SimpleItemTouchHelperCallback(adapter = adapter, itemViewSwipeEnabled = false)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rvSections)
        itemTouchHelper = touchHelper

        btnSave.setOnClickListener {
            dismiss()
        }
    }

    // The best place to save the changes
    override fun onStop() {
        saveChanges()
        super.onStop()
    }

    private fun saveChanges() {
        dialog ?: return
        val adapter = binding.rvSections.adapter as? LibrarySectionAdapter
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