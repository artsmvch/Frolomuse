package com.frolo.muse.ui.main.settings.library.filter

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.SongType
import kotlinx.android.synthetic.main.item_library_song_filter_checkbox.view.*
import kotlin.properties.Delegates


class LibrarySongFilterAdapter(
    private val onItemCheckedChange: (item: SongFilterItem, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<LibrarySongFilterAdapter.ViewHolder>() {

    var items: List<SongFilterItem> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflateChild(R.layout.item_library_song_filter_checkbox)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), CompoundButton.OnCheckedChangeListener {

        private val textView: TextView get() = itemView.tv_title
        private val checkBoxView: CheckBox get() = itemView.chb_checkbox

        init {
            itemView.setOnClickListener {
                checkBoxView.toggle()
            }
        }

        fun bind(item: SongFilterItem) {
            val titleResId = when (item.type) {
                SongType.MUSIC -> R.string.library_song_filter_type_music
                SongType.PODCAST -> R.string.library_song_filter_type_podcast
                SongType.RINGTONE -> R.string.library_song_filter_type_ringtone
                SongType.ALARM -> R.string.library_song_filter_type_alarm
                SongType.NOTIFICATION -> R.string.library_song_filter_type_notification
                SongType.AUDIOBOOK -> R.string.library_song_filter_type_audiobook
            }
            textView.setText(titleResId)

            checkBoxView.setOnCheckedChangeListener(null)
            checkBoxView.isChecked = item.isChecked
            checkBoxView.setOnCheckedChangeListener(this)
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            items.getOrNull(bindingAdapterPosition)?.also { item ->
                onItemCheckedChange.invoke(item, isChecked)
            }
        }

    }

}