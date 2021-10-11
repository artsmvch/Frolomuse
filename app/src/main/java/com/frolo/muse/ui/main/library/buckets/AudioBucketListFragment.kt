package com.frolo.muse.ui.main.library.buckets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.MediaBucket
import com.frolo.muse.ui.ShotLayoutAnimationController
import com.frolo.muse.ui.base.BackPressHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.RESPermissionObserver
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.buckets.files.AudioBucketFragment
import kotlinx.android.synthetic.main.fragment_audio_bucket_list.*
import kotlinx.android.synthetic.main.fragment_base_list.*


class AudioBucketListFragment : BaseFragment(),
        BucketCallback,
        BackPressHandler,
        FragmentContentInsetsListener {

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
        context?.also { safeContext ->
            RESPermissionObserver.observe(safeContext, this) {
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
        val transaction = childFragmentManager.beginTransaction()
        childFragmentManager.fragments?.forEach { childFragment ->
            transaction.remove(childFragment)
        }
        transaction.commitNow()
        layout_list.isVisible = true
    }

    override fun onBackPress(): Boolean {
        val childFragment = childFragmentManager.findFragmentByTag(FRAGMENT_TAG_BUCKET)
        return if (childFragment != null) {
            childFragmentManager.beginTransaction()
                .remove(childFragment)
                .commitNow()
            layout_list.isVisible = true
            true
        } else {
            false
        }
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

    companion object {
        private const val FRAGMENT_TAG_BUCKET = "bucket"
    }

}