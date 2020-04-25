package com.frolo.muse.ui.main.settings.library

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.model.Library
import com.frolo.muse.ui.base.adapter.ItemTouchHelperAdapter
import com.frolo.muse.ui.getSectionName
import com.frolo.muse.ui.main.library.base.BaseAdapter
import kotlinx.android.synthetic.main.item_library_section_switch.view.*
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

    @Deprecated("getItems is deprecated")
    fun getSections() = getItems()

    fun getEnabledStatus(): Map<@Library.Section Int, Boolean> = enabledStatus

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
    ) = with(holder.itemView) {
        val section = getItemAt(position)
        tv_section_name.text = getSectionName(resources, section)
        sw_section_enabled.setOnCheckedChangeListener { _, isChecked ->
            enabledStatus[section] = isChecked
        }
        sw_section_enabled.isChecked = enabledStatus[section] ?: false
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
    }
}