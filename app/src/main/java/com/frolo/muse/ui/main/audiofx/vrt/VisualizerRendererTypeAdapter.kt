package com.frolo.muse.ui.main.audiofx.vrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.frolo.muse.R
import com.frolo.muse.model.VisualizerRendererType


class VisualizerRendererTypeAdapter constructor(
    private val types: Array<VisualizerRendererType>
): BaseAdapter() {

    fun indexOf(item: VisualizerRendererType) = types.indexOf(item)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = if (convertView != null) convertView as TextView
        else {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_visualizer_renderer_type, parent, false) as TextView
        }

        // TODO: localize names of visualizer renderer types
        textView.text = getItem(position).name.toUpperCase()

        return textView
    }

    override fun getItem(position: Int): VisualizerRendererType = types[position]

    override fun getItemId(position: Int): Long = getItem(position).ordinal.toLong()

    override fun getCount(): Int = types.count()

}