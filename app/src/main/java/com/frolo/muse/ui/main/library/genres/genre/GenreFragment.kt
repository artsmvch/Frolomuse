package com.frolo.muse.ui.main.library.genres.genre

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Genre
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.views.showBackArrow
import kotlinx.android.synthetic.main.fragment_genre.*
import kotlinx.android.synthetic.main.include_backdrop_front_list.*


class GenreFragment: AbsSongCollectionFragment() {

    companion object {
        private const val ARG_GENRE = "genre"

        fun newInstance(genre: Genre) = GenreFragment()
                .withArg(ARG_GENRE, genre)
    }

    override val viewModel: GenreViewModel by lazy {
        val genre = requireArguments().getSerializable(ARG_GENRE) as Genre
        val vmFactory = GenreVMFactory(requireApp().appComponent, genre)
        ViewModelProviders.of(this, vmFactory)
                .get(GenreViewModel::class.java)
    }

    override val adapter: SongAdapter by lazy {
        SongAdapter(Glide.with(this)).apply {
            setHasStableIds(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_genre, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@GenreFragment.adapter
            decorateAsLinear()
        }

        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions)
            supportActionBar?.apply {
                showBackArrow()
                subtitle = getString(R.string.genre)
            }
        }
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
        pb_loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        layout_list_placeholder.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            mediaItemCount.observeNonNull(owner) { count ->
                tv_title.text = requireContext().resources.getQuantityString(R.plurals.s_songs, count, count)
            }

            title.observeNonNull(owner) { title ->
                (activity as? AppCompatActivity)?.apply {
                    supportActionBar?.title = title
                }
            }
        }
    }
}