package com.frolo.audiofx2.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.ListPopupWindow


internal class SpinnerImpl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.spinnerStyle
): AppCompatSpinner(context, attrs, defStyleAttr) {

    init {
        try {
            // FIXME: need to find a better solution
            val field = AppCompatSpinner::class.java.getDeclaredField("mPopup")
            field.isAccessible = true
            val popup = field.get(this)
            if (popup is ListPopupWindow) {
                popup.setOnItemClickListener { _, _, position, _ ->
                    markUserSelected()
                    setSelection(position)
                    popup.dismiss()
                }
            }
        } catch (e: Throwable) {
            throw e
        }
    }

    override fun performItemClick(view: View?, position: Int, id: Long): Boolean {
        markUserSelected()
        return super.performItemClick(view, position, id)
    }

    private fun markUserSelected() {
        (onItemSelectedListener as? OnItemSelectedListener)?.byUser = true
    }

    abstract class OnItemSelectedListener: AdapterView.OnItemSelectedListener {
        var byUser: Boolean = false

        abstract fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long,
            byUser: Boolean
        )

        final override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onItemSelected(
                parent = parent,
                view = view,
                position = position,
                id = id,
                byUser = byUser
            )
            byUser = false
        }
    }
}