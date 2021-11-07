package com.frolo.muse.ui.main.library.buckets.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.MediaBucket
import com.frolo.muse.model.media.MediaFile
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.ShotLayoutAnimationController
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.castHost
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.buckets.BucketCallback
import com.frolo.muse.ui.smoothScrollToTop
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.fragment_media_file_list.*


class AudioBucketFragment : AbsMediaCollectionFragment<MediaFile>(), FragmentContentInsetsListener {

    private val bucketArg by serializableArg<MediaBucket>(ARG_BUCKET)

    override val viewModel: AudioBucketViewModel by lazy {
        val appComponent = requireFrolomuseApp().appComponent
        val vmFactory = AudioBucketVMFactory(appComponent, bucketArg)
        ViewModelProviders.of(this, vmFactory).get(AudioBucketViewModel::class.java)
    }

    private val adapter: AudioFileAdapter by lazy {
        AudioFileAdapter(provideThumbnailLoader()).apply {
            listener = object : BaseAdapter.Listener<MediaFile> {
                override fun onItemClick(item: MediaFile, position: Int) {
                    viewModel.onItemClicked(item)
                }

                override fun onItemLongClick(item: MediaFile, position: Int) {
                    viewModel.onItemLongClicked(item)
                }

                override fun onOptionsMenuClick(item: MediaFile, position: Int) {
                    viewModel.onOptionsMenuClicked(item)
                }
            }
        }
    }

    private val bucketCallback: BucketCallback? get() = castHost<BucketCallback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_media_file_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        layout_current_bucket.setOnClickListener {
            bucketCallback?.onLeaveBucket()
        }

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AudioBucketFragment.adapter
            addLinearItemMargins()
            layoutAnimation = ShotLayoutAnimationController()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onSetLoading(loading: Boolean) {
        pb_loading.isVisible = loading
    }

    override fun onSubmitList(list: List<MediaFile>) {
        adapter.submitAndRetainPlayState(list)
    }

    override fun onSubmitSelectedItems(selectedItems: Set<MediaFile>) {
        adapter.submitSelection(selectedItems)
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        layout_list_placeholder.isVisible = visible
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
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

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isPlaying.observeNonNull(owner) { isPlaying ->
            adapter.setPlaying(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayState(playingPosition, isPlaying)
        }

        bucket.observe(owner) { currentBucket ->
            tv_current_bucket_name.text = currentBucket?.displayName
        }
    }

    companion object {

        private const val ARG_BUCKET = "bucket"

        fun newInstance(bucket: MediaBucket): Fragment {
            return AudioBucketFragment().withArg(ARG_BUCKET, bucket)
        }
    }

}