package com.frolo.muse.ui.main.library.buckets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.ui.FragmentUtils
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.music.model.MediaBucket
import com.frolo.muse.ui.ScrolledToTop
import com.frolo.muse.ui.ShotLayoutAnimationController
import com.frolo.muse.ui.base.*
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.buckets.files.AudioBucketFragment
import com.frolo.muse.ui.smoothScrollToTop
import kotlinx.android.synthetic.main.fragment_audio_bucket_list.*
import kotlinx.android.synthetic.main.fragment_base_list.*


class AudioBucketListFragment : BaseFragment(),
        BucketCallback,
        BackPressHandler,
        FragmentContentInsetsListener,
        ScrolledToTop {

    private val viewModel: AudioBucketListViewModel by viewModel()

    private val adapter = AudioBucketAdapter()

    private val adapterListener = object : BaseAdapter.Listener<MediaBucket> {
        override fun onItemClick(item: MediaBucket, position: Int) {
            viewModel.onBucketClicked(item)
        }

        override fun onItemLongClick(item: MediaBucket, position: Int) {
            viewModel.onBucketLongClicked(item)
        }

        override fun onOptionsMenuClick(item: MediaBucket, position: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RESPermissionBus.dispatcher.observe(this) {
            // We only should access the view model if the fragment is attached to an activity
            // to prevent unexpected crashes while providing the view model.
            if (FragmentUtils.isAttachedToActivity(this)) {
                viewModel.onRESPermissionGranted()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_audio_bucket_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AudioBucketListFragment.adapter
            addLinearItemMargins()
            layoutAnimation = ShotLayoutAnimationController()
        }

        layout_list.isVisible = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.onActive()
        observeViewModel(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        adapter.listener = adapterListener
        checkBucketListVisibility()
    }

    override fun onStop() {
        super.onStop()
        adapter.listener = null
    }

    override fun onLeaveBucket() {
        leaveBucketImpl()
    }

    override fun onBackPress(): Boolean {
        return leaveBucketImpl()
    }

    private fun leaveBucketImpl(): Boolean {
        val bucketFragment = peekBucketFragment()
        return if (bucketFragment != null) {
            childFragmentManager.beginTransaction()
                .remove(bucketFragment)
                .commitNow()
            layout_list.isVisible = true
            true
        } else {
            false
        }
    }

    private fun peekBucketFragment(): Fragment? {
        return childFragmentManager.findFragmentByTag(FRAGMENT_TAG_BUCKET)
    }

    private fun openBucket(bucket: MediaBucket) {
        val fragment = AudioBucketFragment.newInstance(bucket)
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, FRAGMENT_TAG_BUCKET)
            .commit()

        layout_list.isVisible = false
    }

    private fun checkBucketListVisibility() {
        val hasBucketFragment = childFragmentManager.findFragmentByTag(FRAGMENT_TAG_BUCKET) != null
        layout_list.isVisible = !hasBucketFragment
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { error ->
            toastError(error)
        }

        isLoading.observeNonNull(owner) { isLoading ->
            pb_loading.isVisible = isLoading
        }

        buckets.observe(owner) { buckets ->
            adapter.submit(buckets.orEmpty())
        }

        placeholderVisible.observeNonNull(owner) { isVisible ->
            layout_list_placeholder.isVisible = isVisible
        }

        openBucketEvent.observeNonNull(owner) { bucket ->
            openBucket(bucket)
        }

//        requestRESPermissionEvent.observe(owner) {
//            checkReadPermissionFor {
//                viewModel.onRESPermissionGranted()
//            }
//        }
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
        val bucketFragment = peekBucketFragment()
        if (bucketFragment != null) {
            if (bucketFragment is ScrolledToTop && FragmentUtils.isInForeground(bucketFragment)) {
                bucketFragment.scrollToTop()
            }
        } else {
            rv_list.smoothScrollToTop()
        }
    }

    companion object {
        private const val FRAGMENT_TAG_BUCKET = "bucket"
    }

}