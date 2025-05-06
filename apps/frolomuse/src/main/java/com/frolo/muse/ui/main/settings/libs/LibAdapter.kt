package com.frolo.muse.ui.main.settings.libs

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.frolo.core.ui.inflateChild

import com.frolo.muse.R
import com.frolo.muse.model.lib.Lib


class LibAdapter constructor(
        private val items: List<Lib>
): RecyclerView.Adapter<LibAdapter.LibViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibViewHolder =
        LibViewHolder(parent.inflateChild(R.layout.item_lib))

    override fun onBindViewHolder(holder: LibViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.count()

    class LibViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLibName = itemView.findViewById<TextView>(R.id.tv_lib_name)
        private val tvLibVersion = itemView.findViewById<TextView>(R.id.tv_lib_version)
        private val tvLibCopyright = itemView.findViewById<TextView>(R.id.tv_lib_copyright)
        private val tvLibLicense = itemView.findViewById<TextView>(R.id.tv_lib_license)

        fun bind(item: Lib) {
            tvLibName.text = item.name
            tvLibVersion.text = item.version
            tvLibCopyright.text = item.copyright
            tvLibLicense.text = item.license
        }

    }
}
