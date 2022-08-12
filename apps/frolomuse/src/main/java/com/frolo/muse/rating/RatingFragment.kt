package com.frolo.muse.rating

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.ui.base.BaseFragment


internal class RatingFragment : BaseFragment() {

    private var ratingDialog: Dialog? = null

    private val viewModel: RatingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        ratingEvent.observe(owner) {
            showRatingDialog()
        }
    }

    private fun showRatingDialog() {
        ratingDialog?.dismiss()

        val context = this.context ?: return
        val dialog = RatingDialog(context) { dialog, what ->
            dialog.dismiss()
            when (what) {
                RatingDialog.Button.POSITIVE ->
                    viewModel.onPositiveAnswer()
                RatingDialog.Button.NEGATIVE ->
                    viewModel.onNegativeAnswer()
                RatingDialog.Button.NEUTRAL ->
                    viewModel.onNeutralAnswer()
            }
        }
        ratingDialog = dialog.apply {
            setOnCancelListener { viewModel.onCancel() }
            show()
        }
    }

    companion object {
        private const val FRAGMENT_TAG = "com.frolo.muse.rating:RATING"

        fun install(fragmentManager: FragmentManager) {
            if (!fragmentManager.isStateSaved) {
                val currFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
                if (currFragment == null) {
                    fragmentManager.beginTransaction()
                        .add(RatingFragment(), FRAGMENT_TAG)
                        .commitNow()
                }
            }
        }

        fun uninstall(fragmentManager: FragmentManager) {
            if (!fragmentManager.isStateSaved) {
                val currFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)
                if (currFragment != null) {
                    fragmentManager.beginTransaction()
                        .remove(currFragment)
                        .commitNow()
                }
            }
        }
    }
}