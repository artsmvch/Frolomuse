package com.frolo.muse.ui.main.settings.libs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.frolo.muse.R
import com.frolo.muse.model.lib.Lib

import kotlinx.android.synthetic.main.item_lib.view.*

class LibAdapter constructor(private val items: List<Lib>): androidx.recyclerview.widget.RecyclerView.Adapter<LibAdapter.LibViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): LibViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lib, parent, false)
        return LibViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.count()

    class LibViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(item: Lib) {
            with(itemView) {
                tv_lib_name.text = item.name
                tv_lib_version.text = item.version
                tv_lib_copyright.text = item.copyright
                tv_lib_license.text = item.license
            }
        }
    }
}
