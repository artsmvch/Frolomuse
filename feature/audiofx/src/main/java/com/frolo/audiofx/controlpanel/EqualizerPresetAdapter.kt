package com.frolo.audiofx.controlpanel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.frolo.audiofx.ui.R
import com.frolo.audiofx2.EqualizerPreset
import com.frolo.ui.Screen
import kotlinx.android.synthetic.main.item_equalizer_preset.view.preset_name
import kotlinx.android.synthetic.main.item_equalizer_preset_drop_down.view.*


class EqualizerPresetAdapter constructor(
    private val presets: List<EqualizerPreset>,
    private val onRemoveItem: ((item: EqualizerPreset) -> Unit)? = null
) : BaseAdapter() {

    fun indexOf(item: EqualizerPreset) = presets.indexOf(item)

    override fun getCount(): Int = presets.count()

    override fun getItem(position: Int): EqualizerPreset = presets[position]

    override fun getItemId(position: Int): Long = presets[position].name.hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_equalizer_preset, parent, false)
        } else {
            convertView
        }
        val preset = getItem(position)
        bindView(itemView, preset, isDropDownItem = false)
        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_equalizer_preset_drop_down, parent, false)
        } else {
            convertView
        }
        val preset = getItem(position)
        bindView(itemView, preset, isDropDownItem = true)
        return itemView
    }

    private fun bindView(
        itemView: View,
        preset: EqualizerPreset,
        isDropDownItem: Boolean
    ) = itemView.apply {
        preset_name.text = preset.name
        remove_icon?.apply {
            isVisible = preset.isDeletable
            setOnClickListener { onRemoveItem?.invoke(preset) }
        }
        if (isDropDownItem) {
            val context = itemView.context
            preset_name.updatePadding(
                left = Screen.dp(context, 8f),
                right = if (remove_icon?.isVisible == true) {
                    0
                } else {
                    Screen.dp(context, 16f)
                }
            )
        }
    }

//    @DrawableRes
//    private fun getDrawableIdForPreset(presetName: String): Int {
//        return when (presetName.toLowerCase()) {
//            "normal" -> R.drawable.png_normal
//            "rock" -> R.drawable.png_rock
//            "heavy metal" -> R.drawable.png_heavy_metal
//            "classical" -> R.drawable.png_classical
//            "folk" -> R.drawable.png_folk
//            "flat" -> R.drawable.png_flat
//            "dance" -> R.drawable.png_dance
//            "hip hop" -> R.drawable.png_hip_hop
//            "jazz" -> R.drawable.png_jazz
//            "pop" -> R.drawable.png_pop
//            else -> R.drawable.png_normal
//        }
//    }
//
//    @StringRes
//    private fun getStringIdForPreset(presetName: String): Int {
//        return when (presetName.toLowerCase()) {
//            "normal" -> R.string.preset_normal
//            "rock" -> R.string.preset_rock
//            "heavy metal" -> R.string.preset_heavy_metal
//            "classical" -> R.string.preset_classical
//            "folk" -> R.string.preset_folk
//            "flat" -> R.string.preset_flat
//            "dance" -> R.string.preset_dance
//            "hip hop" -> R.string.preset_hip_hop
//            "jazz" -> R.string.preset_jazz
//            "pop" -> R.string.preset_pop
//            else -> R.string.preset_normal
//        }
//    }
}
