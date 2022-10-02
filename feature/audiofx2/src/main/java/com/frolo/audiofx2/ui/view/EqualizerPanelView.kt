package com.frolo.audiofx2.ui.view

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.frolo.audiofx2.ui.R
import com.frolo.audiofx2.AudioEffect2
import com.frolo.audiofx2.Equalizer
import com.frolo.audiofx2.EqualizerPreset
import com.frolo.equalizerview.impl.SeekBarEqualizerView
import com.frolo.rx.KeyedDisposableContainer
import com.frolo.ui.Screen
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_equalizer_preset.view.preset_name
import kotlinx.android.synthetic.main.item_equalizer_preset_drop_down.view.*


class EqualizerPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr) {
    private val equalizerView: SeekBarEqualizerView
    private val captionTextView: TextView
    private val enableStatusSwatch: SwitchMaterial
    private val switchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        this.equalizer?.isEnabled = isChecked
    }
    private val presetSpinner: SpinnerImpl
    private val spinnerListener = object : SpinnerImpl.OnItemSelectedListener() {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long,
            byUser: Boolean
        ) {
            val adapter = parent?.adapter
            if (adapter is PresetAdapter && byUser) {
                val preset = adapter.getItem(position)
                usePresetAsync(preset)
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }
    private val savePresetButton: View
    private val savePresetClickListener = View.OnClickListener {
        showSavePresetDialog()
    }

    init {
        View.inflate(context, R.layout.merge_equalizer_panel, this)
        equalizerView = findViewById(R.id.equalizer_view)
        captionTextView = findViewById(R.id.caption)
        enableStatusSwatch = findViewById(R.id.enable_status_switch)
        presetSpinner = findViewById(R.id.preset_spinner)
        savePresetButton = findViewById(R.id.save_preset_button)
        // Set up listeners
        enableStatusSwatch.setOnCheckedChangeListener(switchListener)
        presetSpinner.onItemSelectedListener = spinnerListener
        savePresetButton.setOnClickListener(savePresetClickListener)
    }

    private var equalizer: Equalizer? = null
    private val onEnableStatusChangeListener =
        AudioEffect2.OnEnableStatusChangeListener { effect, enabled ->
            equalizerView.isEqualizerUiEnabled = enabled
            setChecked(checked = enabled)
            setPresetChooserEnabled(enabled = enabled)
            setSavePresetButtonEnabled(enabled = enabled)
        }
    private val onPresetUsedListener =
        Equalizer.OnPresetUsedListener { equalizer, preset ->
            setPresetSelection(preset)
        }

    // Async operations
    private val keyedDisposableContainer = KeyedDisposableContainer<String>()

    fun setup(equalizer: Equalizer?) {
        if (this.equalizer == equalizer) {
            // No changes
            return
        }
        this.equalizer?.apply {
            removeOnEnableStatusChangeListener(onEnableStatusChangeListener)
            removeOnPresetUsedListener(onPresetUsedListener)
        }
        keyedDisposableContainer.clear()
        this.equalizer = equalizer
        equalizer?.apply {
            addOnEnableStatusChangeListener(onEnableStatusChangeListener)
            addOnPresetUsedListener(onPresetUsedListener)
        }
        equalizerView.isEqualizerUiEnabled = equalizer?.isEnabled == true
        equalizerView.setup(
            equalizer = equalizer?.let(::AudioFx2EqualizerToEqualizerAdapter),
            animate = isLaidOut
        )
        captionTextView.text = equalizer?.descriptor?.name
        setChecked(checked = equalizer?.isEnabled == true)
        setPresetChooserEnabled(enabled = equalizer?.isEnabled == true)
        setSavePresetButtonEnabled(enabled = equalizer?.isEnabled == true)
        loadPresetsAsync(equalizer)
    }

    private fun loadPresetsAsync(equalizer: Equalizer?) {
        if (equalizer == null) {
            setPresets(emptyList(), -1)
            return
        }
        val source1 = Single.fromCallable { equalizer.getAllPresets() }
            .subscribeOn(Schedulers.io())
        val source2 = Single.fromCallable { equalizer.getCurrentPreset() }
            .subscribeOn(Schedulers.io())
        Single.zip(source1, source2) { presets, selectedOne -> presets to selectedOne}
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pair ->
                val selectionIndex = pair.first.indexOfFirst { preset ->
                    preset.isTheSame(pair.second)
                }
                setPresets(pair.first, selectionIndex)
            }
            .also { disposable ->
                keyedDisposableContainer.add("load_presets_async", disposable)
            }
    }

    private fun setPresets(presets: List<EqualizerPreset>, selectionIndex: Int) {
        val listener = presetSpinner.onItemSelectedListener
        presetSpinner.onItemSelectedListener = null
        presetSpinner.adapter = PresetAdapter(
            initialItems = presets,
            onRemoveItem = ::removePresetAsync
        )
        presetSpinner.setSelection(selectionIndex)
        presetSpinner.onItemSelectedListener = listener
    }

    private fun setPresetSelection(selection: EqualizerPreset) {
        val listener = presetSpinner.onItemSelectedListener
        presetSpinner.onItemSelectedListener = null
        val presets = (presetSpinner.adapter as? PresetAdapter)?.items.orEmpty()
        val selectionIndex = presets.indexOfFirst { preset ->
            preset.isTheSame(selection)
        }
        presetSpinner.setSelection(selectionIndex)
        presetSpinner.onItemSelectedListener = listener
    }

    private fun usePresetAsync(preset: EqualizerPreset) {
        val equalizer = this.equalizer ?: return
        Completable.fromAction { equalizer.usePreset(preset) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .also { disposable ->
                keyedDisposableContainer.add("use_preset_async", disposable)
            }
    }

    private fun removePresetAsync(preset: EqualizerPreset) {
        val equalizer = this.equalizer ?: return
        Completable.fromAction { equalizer.deletePreset(preset) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { loadPresetsAsync(equalizer) }
            .subscribe()
            .also { disposable ->
                keyedDisposableContainer.add("remove_preset_async", disposable)
            }
    }

    private fun showSavePresetDialog() {
        val equalizer = this.equalizer ?: return
        Single.fromCallable { equalizer.getBandLevelsSnapshot() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { levels ->
                SavePresetDialog(
                    context = context,
                    equalizer = equalizer,
                    bandLevels = levels,
                    onPresetSaved = { preset ->
                        (presetSpinner.adapter as? PresetAdapter)?.addItem(preset)
                    }
                ).show()
            }
            .also { disposable ->
                keyedDisposableContainer.add("show_save_preset_dialog", disposable)
            }
    }

    private fun setChecked(checked: Boolean) {
        enableStatusSwatch.apply {
            setOnCheckedChangeListener(null)
            isChecked = checked
            setOnCheckedChangeListener(switchListener)
        }
    }

    private fun setPresetChooserEnabled(enabled: Boolean) {
        presetSpinner.isEnabled = enabled
        presetSpinner.alpha = if (enabled) 1.0f else 0.6f
    }

    private fun setSavePresetButtonEnabled(enabled: Boolean) {
        savePresetButton.isEnabled = enabled
        savePresetButton.alpha = if (enabled) 1.0f else 0.6f
    }
}

private class PresetAdapter(
    initialItems: List<EqualizerPreset>,
    private val onRemoveItem: ((item: EqualizerPreset) -> Unit)? = null
) : BaseAdapter() {
    private val _items = ArrayList<EqualizerPreset>(initialItems)
    val items: List<EqualizerPreset> get() = _items

    fun addItem(preset: EqualizerPreset) {
        _items.add(preset)
        notifyDataSetChanged()
    }

    fun removeItem(preset: EqualizerPreset) {
        if (_items.remove(preset)) {
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int = items.count()

    override fun getItem(position: Int): EqualizerPreset = items[position]

    override fun getItemId(position: Int): Long = items[position].name.hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_equalizer_preset, parent, false)
        } else {
            convertView
        }
        val preset = getItem(position)
        bindView(itemView, preset, isDropDownItem = false)
        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            inflater.inflate(R.layout.item_equalizer_preset_drop_down, parent, false)
        } else {
            convertView
        }
        val preset = getItem(position)
        bindView(itemView, preset, isDropDownItem = true)
        return itemView
    }

    private fun bindView(
        itemView: View,
        preset: EqualizerPreset,
        isDropDownItem: Boolean
    ) = itemView.apply {
        preset_name.text = preset.name
        remove_icon?.apply {
            isVisible = preset.isDeletable
            setOnClickListener { onRemoveItem?.invoke(preset) }
        }
        if (isDropDownItem) {
            val context = itemView.context
            preset_name.updatePadding(
                left = Screen.dp(context, 8f),
                right = if (remove_icon?.isVisible == true) {
                    0
                } else {
                    Screen.dp(context, 16f)
                }
            )
        }
    }
}

private class SavePresetDialog(
    context: Context,
    private val equalizer: Equalizer,
    private val bandLevels: Map<Int, Int>,
    private val onPresetSaved: (EqualizerPreset) -> Unit
): AppCompatDialog(context) {
    private var savePresetDisposable: Disposable? = null

    // Views
    private var nameInputLayout: TextInputLayout? = null
    private var nameEditText: EditText? = null
    private var cancelButton: View? = null
    private var saveButton: View? = null
    private var progress: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_save_preset)
        setUpWindow()
        loadUi()
    }

    private fun setUpWindow() {
        val window = this.window ?: return
        val dialogWidth: Int = (Screen.getScreenWidth(context) * 0.90).toInt()
        val dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT
        window.setLayout(dialogWidth, dialogHeight)
    }

    private fun loadUi() {
        window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
        nameInputLayout = findViewById<TextInputLayout>(R.id.name_input_layout)
        nameEditText = findViewById<EditText>(R.id.name_edit_text)?.also { editText ->
            editText.requestFocus()
        }
        cancelButton = findViewById<View>(R.id.cancel_button)?.also { button ->
            button.setOnClickListener { dismiss() }
        }
        saveButton = findViewById<View>(R.id.save_button)?.also { button ->
            button.setOnClickListener {
                savePresetAsync()
            }
        }
        progress = findViewById<View>(R.id.progress)?.also { progress ->
            progress.setOnClickListener { /* stub */ }
        }
    }

    private fun showError(error: Throwable) {
        nameInputLayout?.error = error.localizedMessage
    }

    private fun savePresetAsync() {
        val name = nameEditText?.text?.toString().orEmpty()
        Single.fromCallable { equalizer.createPreset(name, bandLevels) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { err -> showError(err) }
            .doOnSubscribe { progress?.isVisible = true }
            .doFinally { progress?.isVisible = false }
            .subscribe { preset ->
                onPresetSaved.invoke(preset)
                dismiss()
            }
            .also { disposable ->
                savePresetDisposable?.dispose()
                savePresetDisposable = disposable
            }
    }

}