package com.frolo.muse.ui.main.library.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.databinding.FragmentBaseListBinding
import com.frolo.music.model.Media
import com.frolo.muse.ui.ShotLayoutAnimationController
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.smoothScrollToTop


abstract class SimpleMediaCollectionFragment <E: Media>:
        AbsMediaCollectionFragment<E>(),
        FragmentContentInsetsListener {

    private var _binding: FragmentBaseListBinding? = null
    private val binding: FragmentBaseListBinding get() = _binding!!

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
    ): View? {
        _binding = FragmentBaseListBinding.inflate(inflater)
        return _binding?.root
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setting up the list view
        onDecorateListView(binding.rvList)
        // Setting up the placeholder view
        onDecoratePlaceholderView(binding.layoutListPlaceholder.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun requireListView(): RecyclerView {
        return requireNotNull(binding.rvList)
    }

    protected open fun onDecorateListView(listView: RecyclerView) {
        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(context)
        listView.addLinearItemMargins()
        listView.layoutAnimation = ShotLayoutAnimationController()
    }

    protected open fun onDecoratePlaceholderView(view: View) {
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
        binding.pbLoading.root.visibility = if (loading) View.VISIBLE else View.GONE
    }

    final override fun onSubmitList(list: List<E>) {
        adapter.submit(list)
    }

    final override fun onSubmitSelectedItems(selectedItems: Set<E>) {
        adapter.submitSelection(selectedItems)
    }

    final override fun onSetPlaceholderVisible(visible: Boolean) {
        binding.layoutListPlaceholder.root.visibility = if (visible) View.VISIBLE else View.GONE
    }

    final override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                binding.rvList.setPadding(left, top, right, bottom)
                binding.rvList.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }

    override fun scrollToTop() {
        binding.rvList.smoothScrollToTop()
    }
}