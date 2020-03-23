package com.frolo.muse.ui.main.library.myfiles

import android.os.Bundle
import android.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.mediascan.MediaScanService
import com.frolo.muse.model.media.MyFile
import com.frolo.muse.ui.base.BackPressHandler
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.getNameAsRootString
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.views.Anim
import kotlinx.android.synthetic.main.fragment_my_file_list.*


class MyFileListFragment: AbsMediaCollectionFragment<MyFile>(),
        BackPressHandler,
        NoClipping {

    companion object {
        // Factory
        fun newInstance() = MyFileListFragment()
    }

    override val viewModel: MyFileListViewModel by viewModel()

    private val adapter: MyFileAdapter by lazy {
        MyFileAdapter().apply {
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
    ): View = inflater.inflate(R.layout.fragment_my_file_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MyFileListFragment.adapter
            decorateAsLinear()
        }
        cl_parent_file.setOnClickListener { viewModel.onRootClicked() }
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
            Anim.fadeIn(pb_loading)
        } else {
            Anim.fadeOut(pb_loading)
        }
    }

    override fun onSubmitList(list: List<MyFile>) {
        val playingPosition = viewModel.playingPosition.value ?: -1
        val isPlaying = viewModel.isPlaying.value ?: false
        adapter.submit(list, playingPosition, isPlaying)
    }

    override fun onSubmitSelectedItems(selectedItems: Set<MyFile>) {
        adapter.submitSelection(selectedItems)
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        layout_list_placeholder.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isPlaying.observeNonNull(owner) { isPlaying ->
            adapter.setPlayingState(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayingPositionAndState(playingPosition, isPlaying)
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
        tv_parent_file_name.text = root.getNameAsRootString()
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                safeView.setPadding(left, top, right, bottom)
                safeView.clipToPadding = false
            }
        }
    }
}