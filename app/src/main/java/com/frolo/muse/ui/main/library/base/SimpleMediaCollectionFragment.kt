package com.frolo.muse.ui.main.library.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.model.media.Media
import com.frolo.muse.ui.ShotLayoutAnimationController
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.main.addLinearItemMargins
import kotlinx.android.synthetic.main.fragment_base_list.*


abstract class SimpleMediaCollectionFragment <E: Media>:
        AbsMediaCollectionFragment<E>(),
        NoClipping {

    abstract val adapter: BaseAdapter<E, *>

    private val adapterListener =
        object : BaseAdapter.Listener<E> {
            override fun onItemClick(item: E, position: Int) {
                viewModel.onItemClicked(item)
            }

            override fun onItemLongClick(item: E, position: Int) {
                viewModel.onItemLongClicked(item)
            }

            override fun onOptionsMenuClick(item: E, position: Int) {
                viewModel.onOptionsMenuClicked(item)
            }
        }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_base_list, container, false)

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setting up the list
        onDecorateList(rv_list)
        // Setting up the placeholder
        onDecoratePlaceholder(layout_list_placeholder)
    }

    protected fun requireList(): RecyclerView {
        return requireNotNull(rv_list)
    }

    protected open fun onDecorateList(list: RecyclerView) {
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(context)
        list.addLinearItemMargins()
        list.layoutAnimation = ShotLayoutAnimationController()
    }

    protected open fun onDecoratePlaceholder(list: View) {
    }

    override fun onStart() {
        super.onStart()
        adapter.listener = adapterListener
    }

    override fun onStop() {
        super.onStop()
        adapter.listener = null
    }

    final override fun onSetLoading(loading: Boolean) {
        pb_loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    final override fun onSubmitList(list: List<E>) {
        adapter.submit(list)
    }

    final override fun onSubmitSelectedItems(selectedItems: Set<E>) {
        adapter.submitSelection(selectedItems)
    }

    final override fun onSetPlaceholderVisible(visible: Boolean) {
        layout_list_placeholder.visibility = if (visible) View.VISIBLE else View.GONE
    }

    final override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                rv_list.setPadding(left, top, right, bottom)
                rv_list.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }
}