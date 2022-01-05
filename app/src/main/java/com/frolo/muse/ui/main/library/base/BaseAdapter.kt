package com.frolo.muse.ui.main.library.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frolo.debug.DebugUtils
import java.util.*
import kotlin.collections.ArrayList


abstract class BaseAdapter<E, VH> constructor(
    private val itemCallback: DiffUtil.ItemCallback<E>? = null
): RecyclerView.Adapter<VH>() where VH: BaseAdapter.BaseViewHolder {

    private data class Node<E>(val item: E, val selected: Boolean = false)

    interface Listener<E> {
        fun onItemClick(item: E, position: Int)
        fun onItemLongClick(item: E, position: Int)
        fun onOptionsMenuClick(item: E, position: Int)
    }

    var listener: Listener<E>? = null
    private var nodes = ArrayList<Node<E>>()

    fun getSnapshot(): List<E> = nodes.map { node -> node.item }

    /**
     * Use this method carefully.
     * Call it only if no data changed, but the items should be rebound.
     * For instance, if you want to reload images or something else.
     */
    fun forceResubmit() {
        notifyDataSetChanged()
    }

    fun submit(list: List<E>) {
        val newNodes = ArrayList<Node<E>>(list.size).also { newNodeList ->
            list.mapTo(newNodeList) { item -> Node(item, false) }
        }

        submitImpl(newNodes)
    }

    fun submit(list: List<E>, selectedItem: Collection<E>) {
        val newNodes = ArrayList<Node<E>>(nodes.size).also { newNodeList ->
            list.mapTo(newNodeList) { item -> Node(item, selectedItem.contains(item)) }
        }

        submitImpl(newNodes)
    }

    private fun submitImpl(newNodes: ArrayList<Node<E>>) {
        if (itemCallback != null) {
            val callback = NodeCallback(nodes, newNodes, itemCallback)
            val diffResult = DiffUtil.calculateDiff(callback)
            nodes = newNodes
            diffResult.dispatchUpdatesTo(this)
        } else {
            nodes = newNodes
            notifyDataSetChanged()
        }
    }

    fun submitSelection(selectedItems: Collection<E>) {
        // TODO: optimization required
        for (index in nodes.indices) {
            val node = nodes[index]
            val selected = selectedItems.contains(node.item)
            if (node.selected != selected) {
                val newNode = node.copy(selected = selected)
                nodes[index] = newNode
                notifyItemChanged(index, SELECTION_CHANGED_PAYLOAD)
            }
        }
    }

    fun getItemAt(position: Int): E = nodes[position].item

    fun indexOf(item: E) = nodes.indexOfFirst { node -> node.item == item }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < 0 || fromPosition >= nodes.size
                || toPosition < 0 || toPosition >= nodes.size) {
            // Positions are out of bounds
            val error = IllegalArgumentException("Failed to move item from position $fromPosition " +
                    "to position $toPosition; list size is ${nodes.size}")
            DebugUtils.dumpOnMainThread(error)
            return
        }
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(nodes, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(nodes, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    operator fun set(position: Int, item: E) {
        nodes[position] = nodes[position].copy(item = item)
        notifyItemChanged(position, SELECTION_CHANGED_PAYLOAD)
    }

    fun remove(position: Int) {
        nodes.removeAt(position)
        notifyItemRemoved(position)
    }

    fun add(position: Int, item: E) {
        nodes.add(position, Node(item))
        notifyItemInserted(position)
    }

    fun add(item: E) {
        nodes.add(Node(item))
        notifyItemInserted(nodes.size - 1)
    }

    final override fun getItemCount() = nodes.count()

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return onCreateBaseViewHolder(parent, viewType).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position in 0 until itemCount) {
                    listener?.onItemClick(getItemAt(position), position)
                }
            }
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position in 0 until itemCount) {
                    listener?.onItemLongClick(getItemAt(position), position)
                }
                true
            }
            viewOptionsMenu?.setOnClickListener {
                val position = adapterPosition
                if (position in 0 until itemCount) {
                    listener?.onOptionsMenuClick(getItemAt(position), position)
                }
            }
        }
    }

    abstract fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): VH

    final override fun onBindViewHolder(holder: VH, position: Int) {
        val node = nodes[position]
        onBindViewHolder(holder, position, node.item, node.selected, false)
    }

    final override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        val node = nodes[position]
        val selectionChanged = payloads.contains(SELECTION_CHANGED_PAYLOAD)
        onBindViewHolder(holder, position, node.item, node.selected, selectionChanged)
    }

    abstract fun onBindViewHolder(holder: VH, position: Int, item: E, selected: Boolean, selectionChanged: Boolean)

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract val viewOptionsMenu: View?
    }

    private class NodeCallback<E> constructor(
        private val oldNodes: List<Node<E>>,
        private val newNodes: List<Node<E>>,
        private val itemCallback: DiffUtil.ItemCallback<E>
    ): DiffUtil.Callback() {

        override fun getOldListSize() = oldNodes.size

        override fun getNewListSize() = newNodes.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldNode = oldNodes[oldItemPosition]
            val newNode = newNodes[newItemPosition]
            return itemCallback.areItemsTheSame(oldNode.item, newNode.item)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldNode = oldNodes[oldItemPosition]
            val newNode = newNodes[newItemPosition]
            return oldNode.selected == newNode.selected
                    && itemCallback.areContentsTheSame(oldNode.item, newNode.item)
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldNode = oldNodes[oldItemPosition]
            val newNode = newNodes[newItemPosition]
            return if (oldNode.selected != newNode.selected) {
                SELECTION_CHANGED_PAYLOAD
            } else {
                itemCallback.getChangePayload(oldNode.item, newNode.item)
            }
        }

    }

    companion object {
        private val SELECTION_CHANGED_PAYLOAD = Any()
    }

}
