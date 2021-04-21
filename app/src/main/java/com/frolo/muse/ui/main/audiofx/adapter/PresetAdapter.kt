package com.frolo.muse.ui.main.audiofx.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.frolo.muse.R
import com.frolo.muse.dp2px
import com.frolo.muse.model.preset.CustomPreset
import com.frolo.muse.model.preset.NativePreset
import com.frolo.muse.model.preset.Preset
import kotlinx.android.synthetic.main.item_preset_drop_down.view.*


class PresetAdapter constructor(
    private val presets: List<Preset>,
    private val onRemoveItem: ((item: CustomPreset) -> Unit)? = null
) : BaseAdapter() {

    fun indexOf(item: Preset) = presets.indexOf(item)

    override fun getCount(): Int = presets.count()

    override fun getItem(position: Int): Preset = presets[position]

    override fun getItemId(position: Int): Long = presets[position].name.hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_preset, parent, false)
        } else convertView

        return view.apply {
            val preset = getItem(position)
            bindView(this, preset, false)
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_preset_drop_down, parent, false)
        } else convertView

        return view.apply {
            val preset = getItem(position)
            bindView(this, preset, true)
        }
    }

    private fun bindView(itemView: View, preset: Preset, isDropDownItem: Boolean) = itemView.apply {
        val name = preset.name
        if (preset is NativePreset) {
            imv_preset_icon.isVisible = false
            imv_preset_icon.setImageResource(getDrawableIdForPreset(name))
            tv_preset_name.setText(getStringIdForPreset(name))
        } else {
            imv_preset_icon.isVisible = false
            imv_preset_icon.setImageDrawable(null)
            tv_preset_name.text = name
        }
        btn_remove?.apply {
            visibility = if (preset is CustomPreset) View.VISIBLE else View.GONE
            setOnClickListener {
                (preset as? CustomPreset)?.let { item ->
                    onRemoveItem?.invoke(item)
                }
            }
        }
        if (isDropDownItem) {
            val context = itemView.context
            val paddingLeft = if (imv_preset_icon.isVisible) 0 else 8f.dp2px(context).toInt()
            val paddingRight = if (btn_remove?.isVisible == true) 0 else 16f.dp2px(context).toInt()
            tv_preset_name.updatePadding(
                left = paddingLeft,
                right = paddingRight
            )
        }
    }

    @DrawableRes
    private fun getDrawableIdForPreset(presetName: String): Int {
        return when (presetName.toLowerCase()) {
            "normal" -> R.drawable.png_normal
            "rock" -> R.drawable.png_rock
            "heavy metal" -> R.drawable.png_heavy_metal
            "classical" -> R.drawable.png_classical
            "folk" -> R.drawable.png_folk
            "flat" -> R.drawable.png_flat
            "dance" -> R.drawable.png_dance
            "hip hop" -> R.drawable.png_hip_hop
            "jazz" -> R.drawable.png_jazz
            "pop" -> R.drawable.png_pop
            else -> R.drawable.png_normal
        }
    }

    @StringRes
    private fun getStringIdForPreset(presetName: String): Int {
        return when (presetName.toLowerCase()) {
            "normal" -> R.string.preset_normal
            "rock" -> R.string.preset_rock
            "heavy metal" -> R.string.preset_heavy_metal
            "classical" -> R.string.preset_classical
            "folk" -> R.string.preset_folk
            "flat" -> R.string.preset_flat
            "dance" -> R.string.preset_dance
            "hip hop" -> R.string.preset_hip_hop
            "jazz" -> R.string.preset_jazz
            "pop" -> R.string.preset_pop
            else -> R.string.preset_normal
        }
    }

}
