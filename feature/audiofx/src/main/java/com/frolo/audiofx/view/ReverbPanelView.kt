package com.frolo.audiofx.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.frolo.audiofx.ui.R
import com.frolo.audiofx2.AudioEffect2
import com.frolo.audiofx2.Reverb
import com.google.android.material.slider.RangeSlider
import com.google.android.material.switchmaterial.SwitchMaterial

class ReverbPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr) {
    // Views
    private val captionTextView: TextView
    private val enableStatusSwitch: SwitchMaterial
    private val slider: RangeSlider
    private val switchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        this.effect?.isEnabled = isChecked
    }
    private val changeListener = RangeSlider.OnChangeListener { _, value, fromUser ->
        if (fromUser) {
            usePresetByIndexAsync(value.toInt())
        }
    }

    init {
        View.inflate(context, R.layout.merge_reverb_panel, this)
        captionTextView = findViewById(R.id.caption)
        enableStatusSwitch = findViewById(R.id.enable_status_switch)
        slider = findViewById(R.id.slider)
        // Set up listeners
        enableStatusSwitch.setOnCheckedChangeListener(switchListener)
        slider.addOnChangeListener(changeListener)
    }

    // Effect
    private var effect: Reverb? = null
    private val onEnableStatusChangeListener =
        AudioEffect2.OnEnableStatusChangeListener { effect, isEnabled ->
            setChecked(isEnabled)
        }
    private val onPresetUsedListener = Reverb.OnPresetUsedListener { _, preset ->
        slider.setValues(preset.level.toFloat())
    }

    fun setup(effect: Reverb?) {
        this.effect?.apply {
            removeOnEnableStatusChangeListener(onEnableStatusChangeListener)
            removeOnPresetUsedListener(onPresetUsedListener)
        }
        this.effect = effect
        effect?.apply {
            addOnEnableStatusChangeListener(onEnableStatusChangeListener)
            addOnPresetUsedListener(onPresetUsedListener)
        }
        captionTextView.text = effect?.descriptor?.name
        setChecked(checked = effect?.isEnabled ?: false)
        loadPresetsAsync(effect)
    }

    private fun setChecked(checked: Boolean) {
        enableStatusSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = checked
            setOnCheckedChangeListener(switchListener)
        }
    }

    private fun loadPresetsAsync(effect: Reverb?) {
        if (effect == null) {
            slider.values = emptyList()
            return
        }
        val allPresets = effect.availablePresets.sortedBy { it.level }
        if (allPresets.size < 2) {
            slider.values = emptyList()
            return
        }
        val currentPreset = effect.preset
        slider.valueFrom = allPresets.first().level.toFloat()
        slider.valueTo = allPresets.last().level.toFloat()
        slider.stepSize = 1f
        slider.setValues(currentPreset.level.toFloat())
    }

    private fun usePresetByIndexAsync(index: Int) {
        val effect = this.effect ?: return
        val targetPreset = effect.availablePresets.find { it.level == index }
            ?:return
        effect.preset = targetPreset
    }
}