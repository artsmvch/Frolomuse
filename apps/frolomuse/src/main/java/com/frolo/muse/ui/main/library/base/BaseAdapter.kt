package com.frolo.muse.ui.main.library.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.frolo.debug.DebugUtils
import java.util.*
import kotlin.collections.ArrayList


abstract class BaseAdapter<E, VH> constructor(
    private val itemCallback: DiffUtil.ItemCallback<E>? = null
): RecyclerView.Adapter<VH>() where VH: BaseAdapter.BaseViewHolder {

    private data class Node<E>(val item: E, val selected: Boolean = false)

    interface Listener<E> {
        /**
         * Called when the [item] at [position] is clicked.
         */
        fun onItemClick(item: E, position: Int)

        /**
         * Called when the [item] at [position] is long clicked.
         */
        fun onItemLongClick(item: E, position: Int)

        /**
         * Called when the options menu for the [item] at [position] is clicked.
         */
        fun onOptionsMenuClick(item: E, position: Int)
    }

    var listener: Listener<E>? = null
    private var nodes = ArrayList<Node<E>>()

    // Async list differ
    private val asyncListDiffer: AsyncListDiffer<Node<E>>? by lazy {
        itemCallback?.let(::createAsyncListDiffer)
    }
    // Indicates whether a new list is being submitted in this adapter.
    private var isSubmittingList: Boolean = false
    // Callbacks to be run when a new list is submitted in this adapter. The order matters.
    private val submitCallbacks = LinkedList<Runnable>()

    private fun createAsyncListDiffer(itemCallback: DiffUtil.ItemCallback<E>): AsyncListDiffer<Node<E>> {
        val callback = AdapterListUpdateCallback(this)
        val config = AsyncDifferConfig.Builder(NodeItemCallback(itemCallback))
            .build()
        return AsyncListDiffer(callback, config)
    }

    fun getSnapshot(): List<E> = nodes.map { node -> node.item }

    /**
     * Use this method carefully.
     * Call it only if no data changed, but the items should be rebound.
     * For instance, if you want to reload images or something else.
     */
    @Deprecated("", ReplaceWith("notifyDataSetChanged()"))
    fun forceResubmit() {
        notifyDataSetChanged()
    }

    fun submit(list: List<E>, callback: Runnable = EMPTY_CALLBACK) {
        val newNodes = ArrayList<Node<E>>(list.size).also { newNodeList ->
            list.mapTo(newNodeList) { item -> Node(item, false) }
        }

        submitImpl(newNodes, callback)
    }

    fun submit(list: List<E>, selectedItem: Collection<E>, callback: Runnable = EMPTY_CALLBACK) {
        val newNodes = ArrayList<Node<E>>(nodes.size).also { newNodeList ->
            list.mapTo(newNodeList) { item -> Node(item, selectedItem.contains(item)) }
        }

        submitImpl(newNodes, callback)
    }

    private fun submitImpl(newNodes: ArrayList<Node<E>>, callback: Runnable = EMPTY_CALLBACK) {
        val differ = asyncListDiffer
        if (differ != null) {
            val callbackWrapper = Runnable {
                nodes = newNodes
                isSubmittingList = false
                // Notify callbacks
                submitCallbacks.forEach { it.run() }
                submitCallbacks.clear()
            }
            isSubmittingList = true
            submitCallbacks.add(callback)
            differ.submitList(newNodes, callbackWrapper)
        } else {
            isSubmittingList = true
            nodes = newNodes
            notifyDataSetChanged()
            isSubmittingList = false
            // Notify callbacks
            submitCallbacks.add(callback)
            submitCallbacks.forEach { it.run() }
            submitCallbacks.clear()
        }
    }

    /**
     * Runs [callback] when the dataset has been submitted and is stable
     * meaning that there is no pending list to be displayed.
     *
     * If a diff result is being calculated against a list previously submitted
     * using the [submitImpl] method, then the callback will fire when the result
     * dispatches updates to this adapter.
     *
     * This method is useful when you need to ensure that the adapter updates
     * in a consistent manner, namely the order of updates.
     */
    protected fun runOnSubmit(callback: Runnable) {
        if (isSubmittingList) {
            submitCallbacks.add(callback)
        } else {
            callback.run()
        }
    }

    fun submitSelection(selectedItems: Collection<E>) = runOnSubmit {
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

    protected fun moveItem(fromPosition: Int, toPosition: Int) = runOnSubmit {
        if (fromPosition < 0 || fromPosition >= nodes.size
                || toPosition < 0 || toPosition >= nodes.size) {
            // Positions are out of bounds
            val error = IllegalArgumentException("Failed to move item from position $fromPosition " +
                    "to position $toPosition; list size is ${nodes.size}")
            DebugUtils.dumpOnMainThread(error)
            return@runOnSubmit
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

    protected fun removeItemAt(position: Int) = runOnSubmit {
        if (nodes.indices.contains(position)) {
            nodes.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    final override fun getItemCount() = nodes.count()

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return onCreateBaseViewHolder(parent, viewType).apply {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position in 0 until itemCount) {
                    listener?.onItemClick(getItemAt(position), position)
                }
            }
            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position in 0 until itemCount) {
                    listener?.onItemLongClick(getItemAt(position), position)
                }
                true
            }
            viewOptionsMenu?.setOnClickListener {
                val position = bindingAdapterPosition
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

    private class NodeItemCallback<E>(
        private val backing: DiffUtil.ItemCallback<E>
    ): DiffUtil.ItemCallback<Node<E>>() {

        override fun areItemsTheSame(oldNode: Node<E>, newNode: Node<E>): Boolean {
            return backing.areItemsTheSame(oldNode.item, newNode.item)
        }

        override fun areContentsTheSame(oldNode: Node<E>, newNode: Node<E>): Boolean {
            return oldNode.selected == newNode.selected
                    && backing.areContentsTheSame(oldNode.item, newNode.item)
        }

        override fun getChangePayload(oldNode: Node<E>, newNode: Node<E>): Any? {
            return if (oldNode.selected != newNode.selected) {
                SELECTION_CHANGED_PAYLOAD
            } else {
                backing.getChangePayload(oldNode.item, newNode.item)
            }
        }
    }

    companion object {
        private val SELECTION_CHANGED_PAYLOAD = Any()

        private val EMPTY_CALLBACK = Runnable { /* nothing */ }
    }

}
