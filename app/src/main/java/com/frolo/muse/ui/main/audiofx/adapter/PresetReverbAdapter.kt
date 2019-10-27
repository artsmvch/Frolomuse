package com.frolo.muse.ui.main.audiofx.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.frolo.muse.R


class PresetReverbAdapter constructor(
        private val presetReverbIndexesAndNames: List<Pair<Short, String>>
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_preset_reverb, parent, false) as TextView
        } else convertView as TextView

        val item = getItem(position)
        view.text = item.second
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_preset_reverb_drop_down, parent, false) as TextView
        } else convertView as TextView

        val item = getItem(position)
        view.text = item.second
        return view
    }

    override fun getItem(position: Int): Pair<Short, String> = presetReverbIndexesAndNames[position]

    override fun getItemId(position: Int): Long = presetReverbIndexesAndNames[position].first.toLong()

    override fun getCount(): Int = presetReverbIndexesAndNames.size

}