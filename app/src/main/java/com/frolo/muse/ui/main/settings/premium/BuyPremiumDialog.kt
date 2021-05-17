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
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.billing.TrialStatus
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.castHost
import com.frolo.muse.views.RecyclerViewDividers
import kotlinx.android.synthetic.main.dialog_buy_premium.*
import java.util.concurrent.TimeUnit


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

        btn_premium_button.setOnClickListener {
            viewModel.onButtonClicked()
        }

        RecyclerViewDividers.attach(
            listView = list_benefits,
            topDivider = view_top_divider,
            bottomDivider = view_bottom_divider
        )
    }

    private fun animateTrialActivation(dialog: Dialog) = with(dialog) {
        fl_trial_activation_container.setOnTouchListener { _, _ -> true }
        fl_trial_activation_container.isVisible = true
        fl_trial_activation_container.alpha = 0f
        fl_trial_activation_container.animate()
            .alpha(1f)
            .setDuration(200L)
            .start()

        cv_trial_activation.setChecked(checked = true, animate = true)

        dismissDelayed(1000L)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            if (BuildConfig.DEBUG) postError(err)
        }

        closeEvent.observe(owner) {
            dismiss()
        }

        showTrialActivationAndCloseEvent.observe(owner) {
            dialog?.apply { animateTrialActivation(this) } ?: dismiss()
        }

        isLoading.observeNonNull(owner) { isLoading ->
            dialog?.apply {
                val transition = Fade().apply {
                    duration = 200L
                }
                TransitionManager.beginDelayedTransition(layout_root, transition)
                inc_progress_overlay.isVisible = isLoading
            }
        }

        premiumStatus.observe(owner) { premiumStatus ->
            dialog?.apply {
                val productDetails = premiumStatus?.productDetails
                val trialStatus = premiumStatus?.trialStatus
                val activatePremium = premiumStatus?.activatePremium ?: false
                when {
                    activatePremium -> {
                        val trialDurationMillis = (trialStatus as? TrialStatus.Available)?.durationMillis
                        if (trialDurationMillis != null) {
                            val days: Int = TimeUnit.MILLISECONDS
                                .toDays(trialDurationMillis)
                                .coerceAtLeast(1)
                                .toInt()
                            val text = getString(R.string.premium_trial_desc_s, days)
                            tv_premium_caption.text = text
                        } else {
                            tv_premium_caption.text = null
                        }
                        tv_premium_caption.isVisible = true
                        btn_premium_button.setText(R.string.activate_premium_trial)
                    }

                    productDetails != null -> {
                        tv_premium_caption.setText(R.string.one_time_purchase)
                        btn_premium_button.text = getString(R.string.buy_premium_for_s, productDetails.price)
                    }

                    else -> {
                        tv_premium_caption.setText(R.string.one_time_purchase)
                        btn_premium_button.setText(R.string.buy)
                    }
                }
            }
        }

        isButtonEnabled.observeNonNull(owner) { isEnabled ->
            dialog?.apply {
                btn_premium_button.isEnabled = isEnabled
            }
        }
    }

    @Deprecated("Will not be called")
    interface OnBuyPremiumClickListener {
        fun onBuyPremiumClick()
    }

    companion object {

        fun newInstance(): DialogFragment = BuyPremiumDialog()
    }

}