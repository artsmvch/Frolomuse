package com.frolo.muse.ui.main.audiofx.preset

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.preset.CustomPreset
import com.frolo.muse.ui.main.library.base.inputname.AbsInputNameDialog


class SavePresetDialog : AbsInputNameDialog() {

    companion object {
        private const val ARG_LEVELS = "levels"

        // Factory
        fun newInstance(levels: ShortArray): SavePresetDialog {
            return SavePresetDialog().apply {
                arguments = Bundle().apply {
                    putShortArray(ARG_LEVELS, levels)
                }
            }
        }

        private fun getLevelsArg(args: Bundle) = args.getShortArray(ARG_LEVELS) as ShortArray
    }

    private val viewModel: SavePresetViewModel by lazy {
        val bandLevels = getLevelsArg(requireArguments())
        val vmFactory = SavePresetVMFactory(requireApp().appComponent, bandLevels)
        ViewModelProviders.of(this, vmFactory)
                .get(SavePresetViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isSavingPreset.observeNonNull(owner) { isSaving ->
            setIsLoading(isSaving)
        }

        presetSavedEvent.observeNonNull(owner) { preset ->
            onPresetSaved(preset)
        }

        savingError.observeNonNull(owner) { err ->
            displayInputError(err)

        }

        error.observeNonNull(owner) { err ->
            displayError(err)
        }
    }

    private fun onPresetSaved(preset: CustomPreset) {
        postLongMessage(R.string.saved)
        PresetSavedEvent.dispatch(requireContext(), preset)
        dismiss()
    }

    override fun onGetTitle() = getString(R.string.save_preset)

    override fun onGetHint() = getString(R.string.preset_name)

    override fun onSaveButtonClick(name: String) {
        viewModel.onSaveButtonClicked(name)
    }
}