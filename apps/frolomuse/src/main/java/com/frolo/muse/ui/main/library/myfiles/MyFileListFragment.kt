package com.frolo.muse.ui.main.library.myfiles

import android.os.Bundle
import android.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.mediascan.MediaScanService
import com.frolo.muse.databinding.FragmentMyFileListBinding
import com.frolo.music.model.MyFile
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.ShotLayoutAnimationController
import com.frolo.muse.ui.base.OnBackPressedHandler
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.getNameAsRootString
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.smoothScrollToTop
import com.frolo.muse.views.Anim


@Deprecated(
    message = "MyFile is replaced with MediaFile",
    replaceWith = ReplaceWith("AudioBucketListFragment")
)
class MyFileListFragment: AbsMediaCollectionFragment<MyFile>(),
        OnBackPressedHandler,
        FragmentContentInsetsListener {

    private var _binding: FragmentMyFileListBinding? = null
    private val binding: FragmentMyFileListBinding get() = _binding!!

    override val viewModel: MyFileListViewModel by viewModel()

    private val adapter: MyFileAdapter by lazy {
        MyFileAdapter(provideThumbnailLoader()).apply {
            listener = object : BaseAdapter.Listener<MyFile> {
                override fun onItemClick(item: MyFile, position: Int) {
                    viewModel.onItemClicked(item)
                }

                override fun onItemLongClick(item: MyFile, position: Int) {
                    viewModel.onItemLongClicked(item)
                }

                override fun onOptionsMenuClick(item: MyFile, position: Int) {
                    viewModel.onOptionsMenuClicked(item)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyFileListBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.includeBaseList.rvList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MyFileListFragment.adapter
            addLinearItemMargins()
            layoutAnimation = ShotLayoutAnimationController()
        }

        binding.clParentFile.setOnClickListener {
            viewModel.onRootClicked()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_abs_media_collection, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_sort) {
            viewModel.onSortOrderOptionSelected()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onSetLoading(loading: Boolean) {
        if (loading) {
            Anim.fadeIn(binding.includeBaseList.pbLoading.root)
        } else {
            Anim.fadeOut(binding.includeBaseList.pbLoading.root)
        }
    }

    override fun onSubmitList(list: List<MyFile>) {
        adapter.submitAndRetainPlayState(list)
    }

    override fun onSubmitSelectedItems(selectedItems: Set<MyFile>) {
        adapter.submitSelection(selectedItems)
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        binding.includeBaseList.layoutListPlaceholder.root.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isPlaying.observeNonNull(owner) { isPlaying ->
            adapter.setPlaying(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayState(playingPosition, isPlaying)
        }

        root.observeNonNull(owner) { root ->
            onDisplayRoot(root)
        }

        isCollectingSongs.observeNonNull(owner) { isCollecting ->
            if (isCollecting) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }

        showFolderSetDefaultMessageEvent.observe(owner) {
            toastLongMessage(R.string.folder_is_default_message)
        }

        showFolderAddedToHiddenMessageEvent.observeNonNull(owner) { count ->
            if (count > 1) {
                toastLongMessage(R.string.message_multiple_files_hidden)
            } else {
                toastLongMessage(R.string.message_one_file_hidden)
            }
        }

        scanFilesEvent.observeNonNull(owner) { targetFiles ->
            checkReadPermissionFor {
                context?.also { safeContext ->
                    MediaScanService.start(safeContext, targetFiles)
                }
            }
        }
    }

    private fun onDisplayRoot(root: MyFile) {
        binding.tvParentFileName.text = root.getNameAsRootString()
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

    companion object {

        // Factory
        fun newInstance() = MyFileListFragment()

    }

}