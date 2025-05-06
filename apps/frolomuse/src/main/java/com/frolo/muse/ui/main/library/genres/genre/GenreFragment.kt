package com.frolo.muse.ui.main.library.genres.genre

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.ui.StyleUtils
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.FragmentGenreBinding
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.Genre
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.setupNavigation
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.confirmShortcutCreation
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.smoothScrollToTop
import com.frolo.muse.ui.toString
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.abs
import kotlin.math.pow


class GenreFragment: AbsSongCollectionFragment<Song>(), FragmentContentInsetsListener {
    
    private var _binding: FragmentGenreBinding? = null
    private val binding: FragmentGenreBinding get() = _binding!!

    override val viewModel: GenreViewModel by lazy {
        val genre = requireArguments().getSerializable(ARG_GENRE) as Genre
        val vmFactory = GenreVMFactory(activityComponent, activityComponent, genre)
        ViewModelProviders.of(this, vmFactory).get(GenreViewModel::class.java)
    }

    override val adapter by lazy { SongAdapter<Song>(provideThumbnailLoader()) }

    private val backdropCornerRadius: Float by lazy {
        resources.getDimension(R.dimen.backdrop_large_tongue_corner_radius)
    }

    private val onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val scrollFactor: Float = abs(verticalOffset.toFloat() / (binding.viewBackdrop.measuredHeight))

            (binding.viewBackdrop.background as? MaterialShapeDrawable)?.apply {
                val poweredScrollFactor = scrollFactor.pow(2)
                val cornerRadius = backdropCornerRadius * (1 - poweredScrollFactor)
                this.shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setBottomRightCorner(CornerFamily.ROUNDED, cornerRadius)
                    .build()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGenreBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(binding.tbActions)

        binding.tbActions.apply {
            inflateMenu(R.menu.fragment_genre)
            setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.action_create_shortcut) {
                    viewModel.onCreateGenreShortcutActionSelected()
                }

                if (menuItem.itemId == R.id.action_sort) {
                    viewModel.onSortOrderOptionSelected()
                }

                true
            }
        }

        binding.includeBaseList.rvList.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@GenreFragment.adapter
            addLinearItemMargins()
        }

        binding.btnPlay.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        binding.appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener)

        binding.viewBackdrop.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(StyleUtils.resolveColor(view.context,
                com.google.android.material.R.attr.colorPrimary))
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setBottomRightCorner(CornerFamily.ROUNDED, backdropCornerRadius)
                .build()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        binding.appBarLayout.removeOnOffsetChangedListener(onOffsetChangedListener)
        super.onDestroyView()
        _binding = null
    }

    override fun onSetLoading(loading: Boolean) {
        binding.includeBaseList.pbLoading.root.visibility = 
            if (loading) View.VISIBLE else View.GONE
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        binding.includeBaseList.layoutListPlaceholder.root.visibility = 
            if (visible) View.VISIBLE else View.GONE
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        songCountWithTotalDuration.observe(owner) { songCountWithTotalDuration ->
            binding.tvGenreInfo.text = songCountWithTotalDuration?.toString(resources)
        }

        title.observe(owner) { title ->
            binding.tvGenreName.text = title
        }

        confirmGenreShortcutCreationEvent.observeNonNull(owner) { genre ->
            context?.confirmShortcutCreation(genre) {
                viewModel.onCreateGenreShortcutActionConfirmed()
            }
        }
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
        binding.includeBaseList.rvList?.smoothScrollToTop()
    }

    companion object {
        private const val ARG_GENRE = "genre"

        fun newInstance(genre: Genre) = GenreFragment()
                .withArg(ARG_GENRE, genre)
    }

}