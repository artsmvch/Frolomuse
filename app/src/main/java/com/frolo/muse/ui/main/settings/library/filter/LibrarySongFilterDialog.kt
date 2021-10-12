package com.frolo.muse.ui.main.settings.library.filter

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.ui.main.settings.BaseSettingsDialog


class LibrarySongFilterDialog : BaseSettingsDialog() {

    private val viewModel: LibrarySongFilterViewModel by viewModel()

    //private var listView: RecyclerView? = null
    private val adapter by lazy {
        LibrarySongFilterAdapter { item, isChecked ->
            viewModel.onItemCheckedChange(item, isChecked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.onFirstCreate()
        }
        observeViewModel(this)
    }

    override fun onSetupTitle(titleView: TextView) {
        titleView.setText(R.string.library_song_filter_title)
    }

    override fun onSetupContentView(contentView: ViewGroup) {
        val listView = RecyclerView(contentView.context)
        listView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        listView.layoutManager = LinearLayoutManager(listView.context)
        listView.adapter = adapter
        contentView.addView(listView)
    }

    override fun onSetupAction(actionView: TextView) {
        actionView.setText(R.string.save)
        actionView.setOnClickListener {
            viewModel.onSaveClicked()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        songFilterItems.observe(owner) { items ->
            adapter.items = items
        }

        closeEvent.observe(owner) {
            dismiss()
        }
    }

    companion object {

        // Factory
        fun newInstance(): LibrarySongFilterDialog {
            return LibrarySongFilterDialog()
        }

    }

}