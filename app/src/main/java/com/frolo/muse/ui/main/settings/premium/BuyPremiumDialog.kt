package com.frolo.muse.ui.main.settings.premium

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.castHost
import kotlinx.android.synthetic.main.dialog_buy_premium.*


class BuyPremiumDialog : BaseDialogFragment() {

    private val viewModel: BuyPremiumViewModel by viewModel()

    private val listener: OnBuyPremiumClickListener? get() = castHost()

    private fun getBenefits(): List<Benefit> {
        val context: Context = requireContext()
        val defaultBenefits = context.resources.getStringArray(R.array.premium_benefits).map { text ->
            Benefit(text, R.drawable.ic_premium_check)
        }
        val supportDevBenefit = Benefit(context.getString(R.string.premium_benefit_support_dev), R.drawable.ic_filled_heart)
        return defaultBenefits + supportDevBenefit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(R.layout.dialog_buy_premium)

            val width = resources.displayMetrics.widthPixels
            setupDialogSize(this, (width * 11) / 12, ViewGroup.LayoutParams.WRAP_CONTENT)

            loadUi(this)
        }
    }

    private fun loadUi(dialog: Dialog) = with(dialog) {
        list_benefits.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BenefitAdapter(getBenefits())
        }

        btn_buy_premium.setOnClickListener {
            val listener = this@BuyPremiumDialog.listener
            if (listener != null) {
                listener.onBuyPremiumClick()
            } else {
                // Let the view model handle it, since the listener is null
                viewModel.onBuyClicked()
            }
            dismiss()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isLoading.observeNonNull(owner) { isLoading ->
            dialog?.apply {
                val transition = Fade().apply {
                    duration = 200L
                }
                TransitionManager.beginDelayedTransition(layout_root, transition)
                inc_progress_overlay.isVisible = isLoading
            }
        }

        productDetails.observe(owner) { productDetails ->
            dialog?.apply {
                if (productDetails != null) {
                    val priceText = productDetails.price
                    btn_buy_premium.text = getString(R.string.buy_premium_for_s, priceText)
                } else {
                    btn_buy_premium.text = getString(R.string.buy)
                }
            }
        }

        isBuyButtonEnabled.observeNonNull(owner) { isEnabled ->
            dialog?.apply {
                btn_buy_premium.isEnabled = isEnabled
            }
        }
    }

    interface OnBuyPremiumClickListener {
        fun onBuyPremiumClick()
    }

    companion object {

        fun newInstance(): DialogFragment = BuyPremiumDialog()
    }

}