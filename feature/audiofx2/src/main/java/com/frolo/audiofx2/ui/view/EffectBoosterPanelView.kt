package com.frolo.audiofx2.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import com.frolo.audiofx2.ui.R
import com.frolo.audiofx2.AudioEffect2
import com.frolo.audiofx2.SimpleAudioEffect2
import com.frolo.audiofx2.ValueRange
import com.google.android.material.switchmaterial.SwitchMaterial


class EffectBoosterPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
    private val effectBoosterView: EffectBoosterView
    private val captionTextView: TextView
    private val enableStatusSwatch: SwitchMaterial

    private val onBoostValueChangeListener =
        EffectBoosterView.OnBoostValueChangeListener { _, boostValue ->
            effect?.also { audioEffect ->
                val newValue: Int = audioEffect.valueRange.let { range ->
                    range.minValue + ((range.maxValue - range.minValue) * boostValue).toInt()
                }
                audioEffect.value = newValue
            }
        }
    private val switchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        this.effect?.isEnabled = isChecked
    }

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        View.inflate(context, R.layout.merge_effect_booster_panel, this)
        effectBoosterView = findViewById(R.id.effect_booster)
        captionTextView = findViewById(R.id.caption)
        enableStatusSwatch = findViewById(R.id.enable_status_switch)
        // Set up listeners
        effectBoosterView.onBoostValueChangeListener = onBoostValueChangeListener
        enableStatusSwatch.setOnCheckedChangeListener(switchListener)
    }

    private var effect: SimpleAudioEffect2? = null
    private val onEnableStatusChangeListener =
        AudioEffect2.OnEnableStatusChangeListener { effect, enabled ->
            effectBoosterView.isEnabled = enabled
            setChecked(checked = enabled)
        }
    private val onEffectValueChangeListener =
        SimpleAudioEffect2.OnEffectValueChangeListener { effect, value ->
            effectBoosterView.boostValue = convertEffectValueToBoostValue(value, effect.valueRange)
        }

    fun setup(effect: SimpleAudioEffect2?) {
        this.effect?.apply {
            removeOnEnableStatusChangeListener(onEnableStatusChangeListener)
            removeOnEffectValueChangeListener(onEffectValueChangeListener)
        }
        this.effect = effect
        effect?.apply {
            addOnEnableStatusChangeListener(onEnableStatusChangeListener)
            addOnEffectValueChangeListener(onEffectValueChangeListener)
        }
        effectBoosterView.boostValue = effect?.let {
            convertEffectValueToBoostValue(it.value, it.valueRange)
        } ?: 0f
        effectBoosterView.isEnabled = effect?.isEnabled == true
        captionTextView.text = effect?.descriptor?.name
        setChecked(checked = effect?.isEnabled == true)
    }

    private fun setChecked(checked: Boolean) {
        enableStatusSwatch.apply {
            setOnCheckedChangeListener(null)
            isChecked = checked
            setOnCheckedChangeListener(switchListener)
        }
    }

    private fun convertEffectValueToBoostValue(value: Int, range: ValueRange): Float {
        return (value - range.minValue).toFloat() / (range.maxValue - range.minValue)
    }
}