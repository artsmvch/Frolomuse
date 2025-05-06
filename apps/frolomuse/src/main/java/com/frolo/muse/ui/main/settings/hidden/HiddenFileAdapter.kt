package com.frolo.muse.ui.main.settings.hidden

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.music.model.MyFile


class HiddenFileAdapter constructor(
    private val onRemoveClick: (item: MyFile) -> Unit
): ListAdapter<MyFile, HiddenFileAdapter.HiddenFileViewHolder>(HiddenFileItemCallback) {

    private fun getItemOrNull(position: Int): MyFile? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiddenFileViewHolder =
            HiddenFileViewHolder(parent.inflateChild(R.layout.item_hidden_file))

    override fun onBindViewHolder(holder: HiddenFileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HiddenFileViewHolder(itemView: View):
            RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

        private val btnRemove: View = itemView.findViewById(R.id.btn_remove)
        private val tvFilename: TextView = itemView.findViewById(R.id.tv_filename)

        init {
            btnRemove.setOnClickListener(this)
        }

        fun bind(item: MyFile) = with(itemView) {
            tvFilename.text = item.javaFile.absolutePath
        }

        override fun onClick(v: View?) {
            getItemOrNull(adapterPosition)?.also(onRemoveClick)
        }

    }

    object HiddenFileItemCallback : DiffUtil.ItemCallback<MyFile>() {

        override fun areItemsTheSame(oldItem: MyFile, newItem: MyFile): Boolean {
            return oldItem.javaFile.absolutePath == newItem.javaFile.absolutePath
        }

        override fun areContentsTheSame(oldItem: MyFile, newItem: MyFile): Boolean {
            return oldItem.isSongFile == newItem.isSongFile
                    && oldItem.javaFile == newItem.javaFile
        }

    }

}