package com.frolo.audiofx2.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.frolo.audiofx2.ui.R
import com.frolo.audiofx2.AudioEffect2
import com.frolo.audiofx2.Reverb
import com.frolo.rx.KeyedDisposableContainer
import com.frolo.ui.StyleUtils
import com.google.android.material.slider.RangeSlider
import com.google.android.material.switchmaterial.SwitchMaterial
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

class ReverbPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.reverbPanelViewStyle
): ConstraintLayout(context, attrs, defStyleAttr, R.style.ReverbPanelView_Default) {
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
            setSliderEnabled(isEnabled = isEnabled)
            setChecked(isEnabled)
        }
    private val onPresetUsedListener = Reverb.OnPresetUsedListener { _, preset ->
        slider.setValues(preset.level.toFloat())
    }
    // Async work
    private val keyedDisposableContainer = KeyedDisposableContainer<String>()

    fun setup(effect: Reverb?) {
        this.effect?.apply {
            removeOnEnableStatusChangeListener(onEnableStatusChangeListener)
            removeOnPresetUsedListener(onPresetUsedListener)
        }
        keyedDisposableContainer.clear()
        this.effect = effect
        effect?.apply {
            addOnEnableStatusChangeListener(onEnableStatusChangeListener)
            addOnPresetUsedListener(onPresetUsedListener)
        }
        captionTextView.text = effect?.descriptor?.name
        setSliderEnabled(isEnabled = effect?.isEnabled ?: false)
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
        val source = Single.fromCallable<List<Reverb.Preset>> {
            effect.availablePresets.sortedBy { it.level }
        }
        source
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { allPresets ->
                if (allPresets.size < 2) {
                    slider.values = emptyList()
                    return@subscribe
                }
                val levelToPresetMap = HashMap<Int, Reverb.Preset>().apply {
                    allPresets.forEach { preset ->
                        put(preset.level, preset)
                    }
                }
                val currentPreset = effect.getCurrentPreset()
                slider.valueFrom = allPresets.first().level.toFloat()
                slider.valueTo = allPresets.last().level.toFloat()
                slider.stepSize = 1f
                slider.setValues(currentPreset.level.toFloat())
                slider.setLabelFormatter { value ->
                    val preset = levelToPresetMap[value.roundToInt()]
                    preset?.name ?: "NULL"
                }
            }
            .also { disposable ->
                keyedDisposableContainer.add("load_presets_async", disposable)
            }
    }

    private fun usePresetByIndexAsync(index: Int) {
        val effect = this.effect ?: return
        val source = Completable.fromAction {
            val targetPreset = effect.availablePresets.find { it.level == index }
                ?: return@fromAction
            effect.usePreset(targetPreset)
        }
        source
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .also { disposable ->
                keyedDisposableContainer.add("use_preset_async", disposable)
            }
    }

    private fun setSliderEnabled(isEnabled: Boolean) {
        slider.isEnabled = isEnabled
        if (isEnabled) {
            val activeColor = StyleUtils.resolveColor(context, R.attr.colorSecondary)
                .let { ColorStateList.valueOf(it) }
            slider.tickActiveTintList = activeColor
            slider.trackActiveTintList = activeColor
            slider.thumbTintList = activeColor
        } else {
            val disabledColor = ContextCompat.getColor(context, R.color.disabled_controller)
                .let { ColorStateList.valueOf(it) }
            slider.tickActiveTintList = disabledColor
            slider.trackActiveTintList = disabledColor
            slider.thumbTintList = disabledColor
        }
    }
}