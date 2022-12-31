package com.frolo.visualizer.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


internal class VisualizerRendererTypeAdapter constructor(
    private val items: Array<VisualizerRendererType>
): BaseAdapter() {

    fun getItemAt(index: Int): VisualizerRendererType = items[index]

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

    override fun getItem(position: Int): VisualizerRendererType = items[position]

    override fun getItemId(position: Int): Long = getItem(position).ordinal.toLong()

    override fun getCount(): Int = items.count()
}