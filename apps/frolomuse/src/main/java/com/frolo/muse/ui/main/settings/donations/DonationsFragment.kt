package com.frolo.muse.ui.main.settings.donations

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.frolo.muse.BuildInfo
import com.frolo.ui.Screen
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.setupNavigation
import com.frolo.muse.util.SimpleLottieAnimationController
import com.frolo.core.ui.recyclerview.FlexibleStaggeredLayoutManager
import com.frolo.muse.databinding.FragmentDonationsBinding
import com.google.android.material.appbar.AppBarLayout


class DonationsFragment : BaseFragment(), FragmentContentInsetsListener {
    private var _binding: FragmentDonationsBinding? = null
    private val binding: FragmentDonationsBinding get() = _binding!!

    private val viewModel: DonationsViewModel by viewModel()

    private val donationItemAdapter: DonationItemAdapter by lazy {
        DonationItemAdapter { donationItem ->
            viewModel.onDonationItemClicked(donationItem)
        }
    }

    private val lottieAnimationController by lazy { SimpleLottieAnimationController(this) }

    private var canDragAppBarLayout: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.onFirstCreate()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDonationsBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(binding.tbActions)

        setupDragCallback()

        binding.rvList.apply {
            layoutManager = getLayoutManager(context)
            adapter = donationItemAdapter
            updatePadding(
                left = Screen.dp(context, 12f),
                right = Screen.dp(context, 12f)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDragCallback() {
        binding.appBarLayout.doOnLayout {
            val behavior = (binding.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior
            if (behavior is AppBarLayout.Behavior) {
                behavior.setDragCallback(
                    object : AppBarLayout.Behavior.DragCallback () {
                        override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                            return canDragAppBarLayout
                        }
                    }
                )
            }
        }
    }

    private fun getLayoutManager(context: Context): RecyclerView.LayoutManager {
        val minSpanCount = 2
        val minItemWidth = Screen.dp(context, 160)
        return FlexibleStaggeredLayoutManager(
            orientation = RecyclerView.VERTICAL,
            minSpanCount = minSpanCount,
            preferredItemSize = minItemWidth
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observe(owner) { error ->
            if (BuildInfo.isDebug()) {
                toastError(error)
            }
        }

        isLoading.observe(owner) { isLoading ->
            (view as? ViewGroup)?.also { rootView ->
                val transition = Fade().apply {
                    duration = 150L
                }
                TransitionManager.beginDelayedTransition(rootView, transition)
            }
            val safeIsLoading = isLoading == true
            binding.tvHeadline.isVisible = !safeIsLoading
            binding.tvInfoText.isVisible = !safeIsLoading
            binding.rvList.isInvisible = safeIsLoading
            binding.pbLoading.isVisible = safeIsLoading
            canDragAppBarLayout = !safeIsLoading
            if (safeIsLoading) {
                binding.appBarLayout.setExpanded(true, false)
            }
        }

        donationItems.observe(owner) { items ->
            donationItemAdapter.items = items
        }

        thanksForDonationEvent.observe(owner) {
            playThanksForDonationAnimation()
        }
    }

    private fun playThanksForDonationAnimation() {
        lottieAnimationController.playAnimation(CONFETTI_ANIM_ASSET_NAME)
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        binding.rvList.apply {
            clipToPadding = false
            updatePadding(bottom = bottom)
        }
    }

    companion object {
        private const val CONFETTI_ANIM_ASSET_NAME = "lottie_animation/confetti.json"

        // Factory
        fun newInstance(): DonationsFragment = DonationsFragment()
    }

}