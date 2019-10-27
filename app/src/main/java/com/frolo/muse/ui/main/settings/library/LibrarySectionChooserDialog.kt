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
import kotlinx.android.synthetic.main.dialog_library_section_chooser.*


class LibrarySectionChooserDialog : BaseDialogFragment(),
        LibrarySectionAdapter.OnDragListener {

    companion object {
        // Factory
        fun newInstance() = LibrarySectionChooserDialog()
    }

    private val preferences: Preferences by prefs()

    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_library_section_chooser)
            setupDialogSize(this)
            initUI(this)
        }
    }

    override fun onDrag(holder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(holder)
    }

    private fun initUI(dialog: Dialog) {
        with(dialog) {

            val sections = preferences.librarySections
            val enabledStatus = sections.associateBy({ it }, { preferences.isLibrarySectionEnabled(it) })
            val adapter = LibrarySectionAdapter(this@LibrarySectionChooserDialog, sections, enabledStatus)

            rv_sections.layoutManager = LinearLayoutManager(context)
            rv_sections.adapter = adapter
            val callback = SimpleItemTouchHelperCallback(adapter = adapter, itemViewSwipeEnabled = false)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(rv_sections)
            itemTouchHelper = touchHelper

            imv_close.setOnClickListener { dismiss() }
        }
    }

    // The best place to save the changes
    override fun onStop() {
        super.onStop()
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
}