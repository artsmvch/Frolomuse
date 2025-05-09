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
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.FragmentMediaFileListBinding
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.MediaBucket
import com.frolo.music.model.MediaFile
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.ShotLayoutAnimationController
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.tryHostAs
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.buckets.BucketCallback
import com.frolo.muse.ui.smoothScrollToTop


class AudioBucketFragment : AbsMediaCollectionFragment<MediaFile>(), FragmentContentInsetsListener {
    private var _binding: FragmentMediaFileListBinding? = null
    private val binding: FragmentMediaFileListBinding get() = _binding!!

    private val bucketArg by serializableArg<MediaBucket>(ARG_BUCKET)

    override val viewModel: AudioBucketViewModel by lazy {
        val vmFactory = AudioBucketVMFactory(activityComponent, activityComponent, bucketArg)
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

    private val bucketCallback: BucketCallback? get() = tryHostAs<BucketCallback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMediaFileListBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.layoutCurrentBucket.setOnClickListener {
            bucketCallback?.onLeaveBucket()
        }

        binding.includeBaseList.rvList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AudioBucketFragment.adapter
            addLinearItemMargins()
            layoutAnimation = ShotLayoutAnimationController()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onSetLoading(loading: Boolean) {
        binding.includeBaseList.pbLoading.root.isVisible = loading
    }

    override fun onSubmitList(list: List<MediaFile>) {
        adapter.submitAndRetainPlayState(list)
    }

    override fun onSubmitSelectedItems(selectedItems: Set<MediaFile>) {
        adapter.submitSelection(selectedItems)
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        binding.includeBaseList.layoutListPlaceholder.root.isVisible = visible
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                binding.includeBaseList.rvList.setPadding(left, top, right, bottom)
                binding.includeBaseList.rvList.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }

    override fun scrollToTop() {
        binding.includeBaseList.rvList.smoothScrollToTop()
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
            binding.tvCurrentBucketName.text = currentBucket?.displayName
        }
    }

    companion object {

        private const val ARG_BUCKET = "bucket"

        fun newInstance(bucket: MediaBucket): Fragment {
            return AudioBucketFragment().withArg(ARG_BUCKET, bucket)
        }
    }

}