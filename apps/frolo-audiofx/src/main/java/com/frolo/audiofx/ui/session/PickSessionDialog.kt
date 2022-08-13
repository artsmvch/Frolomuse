package com.frolo.audiofx.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.frolo.audiofx.R
import com.frolo.audiofx.di.injectViewModel


class PickSessionDialog : DialogFragment() {

    private val viewModel: PickSessionViewModel by injectViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_pick_session, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // observeViewModel(viewLifecycleOwner)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        audioSessions.observe(owner) { audioSessions ->
            audioSessions.size
        }
    }

    companion object {
        private const val FRAGMENT_TAG = "com.frolo.audiofx.ui.session:pick_session"

        fun newInstance(): PickSessionDialog = PickSessionDialog()

        fun show(manager: FragmentManager): PickSessionDialog {
            return newInstance().also { instance ->
                instance.show(manager, FRAGMENT_TAG)
            }
        }
    }
}