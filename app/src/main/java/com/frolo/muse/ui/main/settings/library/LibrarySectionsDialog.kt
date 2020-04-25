package com.frolo.muse.ui.main.settings.library

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.dialog_library_sections.*


class LibrarySectionsDialog : BaseDialogFragment(),
        LibrarySectionAdapter.OnDragListener {

    private val preferences: Preferences by prefs()

    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_library_sections)
            setupDialogSize(this)
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

        val sections = preferences.librarySections
        val enabledStatus = sections.associateBy({ it }, { preferences.isLibrarySectionEnabled(it) })
        val adapter = LibrarySectionAdapter(this@LibrarySectionsDialog, sections, enabledStatus)

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
            val sections = adapter.getSections()
            val enabledStatus = adapter.getEnabledStatus()
            preferences.librarySections = sections
            for (entry in enabledStatus.entries) {
                preferences.setLibrarySectionEnabled(entry.key, entry.value)
            }
        }
    }

    companion object {

        // Factory
        fun newInstance() = LibrarySectionsDialog()

    }

}