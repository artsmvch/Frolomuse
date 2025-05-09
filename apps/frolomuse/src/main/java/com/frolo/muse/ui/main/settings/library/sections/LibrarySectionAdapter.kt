package com.frolo.muse.ui.main.settings.library.sections

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.model.Library
import com.frolo.muse.ui.base.adapter.ItemTouchHelperAdapter
import com.frolo.muse.ui.getSectionName
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.google.android.material.switchmaterial.SwitchMaterial
import java.lang.ref.WeakReference


class LibrarySectionAdapter constructor(
    onDragListener: OnDragListener,
    sections: List<@Library.Section Int>,
    enabledStatus: Map<@Library.Section Int, Boolean>
): BaseAdapter<@Library.Section Int, LibrarySectionAdapter.LibrarySectionViewHolder>(),
        ItemTouchHelperAdapter {

    interface OnDragListener {
        fun onTouchDragView(holder: RecyclerView.ViewHolder)
        fun onItemMoved(fromPosition: Int, toPosition: Int)
    }

    private val onDragListener = WeakReference(onDragListener)

    private val enabledStatus = enabledStatus.toMutableMap()

    init {
        submit(sections)
    }

    fun getEnabledStatusSnapshot(): Map<@Library.Section Int, Boolean> = enabledStatus.toMap()

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): LibrarySectionViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_library_section_switch, parent, false)

        return LibrarySectionViewHolder(view).apply {
            val dragView = itemView.findViewById<View>(R.id.view_drag_and_drop)
            dragView.setOnTouchListener { _, event ->
                when(event.action) {
                    MotionEvent.ACTION_DOWN ->
                        onDragListener.get()?.onTouchDragView(this@apply)
                }
                false
            }
        }
    }

    override fun onBindViewHolder(
        holder: LibrarySectionViewHolder,
        position: Int,
        item: Int,
        selected: Boolean,
        selectionChanged: Boolean
    ) = with(holder) {
        val section = getItemAt(position)
        tvSectionName.text = getSectionName(itemView.resources, section)
        swSectionEnabled.setOnCheckedChangeListener { _, isChecked ->
            enabledStatus[section] = isChecked
        }
        swSectionEnabled.isChecked = enabledStatus[section] ?: false
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        moveItem(fromPosition, toPosition)
    }

    override fun onDragEndedWithResult(fromPosition: Int, toPosition: Int) {
        onDragListener.get()?.onItemMoved(fromPosition, toPosition)
    }

    override fun onDragEnded() = Unit

    override fun onItemDismiss(position: Int) {
        throw IllegalArgumentException("Item in this adapter cannot be dismissed")
    }

    class LibrarySectionViewHolder(itemView: View): BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = null
        val tvSectionName: TextView = itemView.findViewById(R.id.tv_section_name)
        val swSectionEnabled: SwitchMaterial = itemView.findViewById(R.id.sw_section_enabled)
    }
}