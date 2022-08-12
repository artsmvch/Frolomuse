package com.frolo.core.ui.recyclerview

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class RecyclerViewDividers private constructor(
    private val listView: RecyclerView,
    private val topDivider: View?,
    private val bottomDivider: View?
): RecyclerView.OnScrollListener() {

    private fun checkDividers() {
        val layoutManager: LinearLayoutManager = listView.layoutManager
                as? LinearLayoutManager ?: return

        if (layoutManager.orientation == RecyclerView.VERTICAL) {
            // Check if the list can scroll up
            if (listView.canScrollVertically(-1)) {
                topDivider?.also(::showDivider)
            } else {
                topDivider?.also(::hideDivider)
            }

            // Check if the list can scroll down
            if (listView.canScrollVertically(1)) {
                bottomDivider?.also(::showDivider)
            } else {
                bottomDivider?.also(::hideDivider)
            }
        }
    }

    private fun showDivider(divider: View) {
        divider.visibility = View.VISIBLE
    }

    private fun hideDivider(divider: View) {
        divider.visibility = View.INVISIBLE
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        checkDividers()
    }

    fun attach() {
        // First, removing to avoid duplicate listeners
        listView.removeOnScrollListener(this)
        listView.addOnScrollListener(this)
        checkDividers()
    }

    fun detach() {
        listView.removeOnScrollListener(this)
    }

    companion object {
        fun attach(listView: RecyclerView, topDivider: View? = null, bottomDivider: View? = null): RecyclerViewDividers {
            return RecyclerViewDividers(listView, topDivider, bottomDivider).apply { attach() }
        }
    }

}