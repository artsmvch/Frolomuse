package com.frolo.muse.ui.main.settings.libs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.model.lib.Lib
import com.frolo.muse.ui.base.BaseDialogFragment
import java.util.*


class LicensesDialog : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_licenses)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            setupDialogSize(this, 6 * width / 7, ViewGroup.LayoutParams.WRAP_CONTENT)

            loadUi(this)
        }
    }

    private fun loadUi(dialog: Dialog) = with(dialog) {
        findViewById<RecyclerView>(R.id.rv_libs).apply {
            adapter = LibAdapter(getLibs())
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }

        findViewById<View>(R.id.btn_ok).setOnClickListener {
            dismiss()
        }
    }

    private fun getLibs(): List<Lib> {
        val libs = ArrayList<Lib>()
        val res = resources
        val ta = res.obtainTypedArray(R.array.libs)
        val length = ta.length()
        val array = arrayOfNulls<Array<String>>(length)
        for (i in 0 until length) {
            val id = ta.getResourceId(i, 0)
            if (id > 0) {
                array[i] = res.getStringArray(id)
                val lib = Lib(
                        array[i]?.get(0),
                        null,
                        array[i]?.get(1),
                        array[i]?.get(2),
                        null,
                        array[i]?.get(3))
                libs.add(lib)
            } else {
                // something wrong with the XML
            }
        }
        ta.recycle() // Important!
        return libs
    }

    companion object {

        // Factory
        fun newInstance() = LicensesDialog()
    }

}
