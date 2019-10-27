package com.frolo.muse.ui.main.settings.libs


import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.frolo.muse.R
import com.frolo.muse.model.lib.Lib
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_third_party_libs.*
import java.util.*

class ThirdPartyLibsFragment : BaseDialogFragment() {

    companion object {

        // Factory
        fun newInstance() = ThirdPartyLibsFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_third_party_libs)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            setupDialogSize(this, 6 * width / 7, ViewGroup.LayoutParams.WRAP_CONTENT)

            initUI(this)
        }
    }

    private fun fetchLibs(): List<Lib> {
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

    private fun initUI(dialog: Dialog) {
        with(dialog) {
            rv_libs.apply {
                val libs = fetchLibs()
                adapter = LibAdapter(libs)
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            }
            imv_close.setOnClickListener {
                dismiss()
            }
        }

    }
}
