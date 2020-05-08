package com.frolo.muse.ui.main.audiofx.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.StringRes
import com.frolo.muse.R
import com.frolo.muse.model.reverb.Reverb


class ReverbAdapter constructor(
    private val reverbs: List<Reverb>
) : BaseAdapter() {

    fun indexOf(item: Reverb) = reverbs.indexOf(item)

    private fun Reverb.getName(context: Context): String {
        @StringRes val stringResId: Int = when (this) {
            Reverb.NONE -> R.string.preset_reverb_none
            Reverb.LARGE_HALL -> R.string.preset_reverb_large_hall
            Reverb.LARGE_ROOM -> R.string.preset_reverb_large_room
            Reverb.MEDIUM_HALL -> R.string.preset_reverb_medium_hall
            Reverb.MEDIUM_ROOM -> R.string.preset_reverb_medium_room
            Reverb.PLATE -> R.string.preset_reverb_plate
            Reverb.SMALL_ROOM -> R.string.preset_reverb_small_rooom
        }
        return context.getString(stringResId)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_reverb, parent, false) as TextView
        } else convertView as TextView

        val item = getItem(position)
        view.text = item.getName(view.context)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_reverb_drop_down, parent, false) as TextView
        } else convertView as TextView

        val item = getItem(position)
        view.text = item.getName(view.context)
        return view
    }

    override fun getItem(position: Int): Reverb = reverbs[position]

    override fun getItemId(position: Int): Long {
        return when (getItem(position)) {
            Reverb.NONE -> 1
            Reverb.LARGE_HALL -> 2
            Reverb.LARGE_ROOM -> 3
            Reverb.MEDIUM_HALL -> 4
            Reverb.MEDIUM_ROOM -> 5
            Reverb.PLATE -> 6
            Reverb.SMALL_ROOM -> 7
        }
    }

    override fun getCount(): Int = reverbs.count()

}