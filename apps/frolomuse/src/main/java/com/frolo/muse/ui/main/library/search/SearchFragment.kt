package com.frolo.muse.ui.main.library.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import com.frolo.ui.KeyboardUtils
import com.frolo.muse.R
import com.frolo.arch.support.observe
import com.frolo.music.model.Media
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.search.adapter.MediaAdapter
import com.frolo.muse.ui.smoothScrollToTop
import com.frolo.core.ui.hideKeyboardOnScroll
import com.frolo.muse.rx.disposeOnPauseOf
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.concurrent.TimeUnit


class SearchFragment: AbsMediaCollectionFragment<Media>(), FragmentContentInsetsListener {

    override val viewModel: SearchViewModel by viewModel()

    private val adapter by lazy { MediaAdapter(provideThumbnailLoader()) }

    private val adapterListener =
        object : BaseAdapter.Listener<Media> {
            override fun onItemClick(item: Media, position: Int) {
                viewModel.onItemClicked(item)
            }

            override fun onItemLongClick(item: Media, position: Int) {
                viewModel.onItemLongClicked(item)
            }

            override fun onOptionsMenuClick(item: Media, position: Int) {
                viewModel.onOptionsMenuClicked(item)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_list.apply {
            adapter = this@SearchFragment.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(rv_list.context)
            hideKeyboardOnScroll()
            addItemDecoration(StickyRecyclerHeadersDecoration(this@SearchFragment.adapter))
            // Do not apply margin decoration as it breaks header decoration
        }

        sv_query.apply {
            setOnCloseListener { true } // do NOT allow to close
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false

                override fun onQueryTextChange(query: String?): Boolean {
                    viewModel.onQuerySubmitted(query.orEmpty())
                    return true
                }
            })
            val savedQuery = savedInstanceState?.getString(EXTRA_QUERY)
            setQuery(savedQuery, true)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observerViewModel(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        adapter.listener = adapterListener
    }

    override fun onResume() {
        super.onResume()
        showKeyboardWithDelay()
    }

    private fun showKeyboardWithDelay() {
        Completable.timer(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { KeyboardUtils.show(sv_query) }
            .subscribe()
            .disposeOnPauseOf(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_QUERY, sv_query?.query.toString())
    }

    override fun onStop() {
        super.onStop()
        adapter.listener = null
    }

    override fun onDestroyView() {
        KeyboardUtils.hideFrom(this)
        super.onDestroyView()
    }

    override fun onSetLoading(loading: Boolean) {
        pb_loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSubmitList(list: List<Media>) {
        adapter.submit(list)
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        layout_list_placeholder.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onSubmitSelectedItems(selectedItems: Set<Media>) {
        adapter.submitSelection(selectedItems)
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observerViewModel(owner: LifecycleOwner) = with(viewModel) {
        query.observe(owner) { query: String? ->
            adapter.query = query.orEmpty()
        }
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                rv_list.setPadding(left, top, right, bottom)
                rv_list.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }

    override fun scrollToTop() {
        rv_list?.smoothScrollToTop()
    }

    companion object {
        private const val EXTRA_QUERY = "query"

        // Factory
        fun newInstance() = SearchFragment()
    }

}