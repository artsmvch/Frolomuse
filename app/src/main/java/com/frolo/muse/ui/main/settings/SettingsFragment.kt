package com.frolo.muse.ui.main.settings

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.frolo.muse.BuildConfig
import com.frolo.muse.Features
import com.frolo.muse.R
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.di.activityComponent
import com.frolo.muse.logger.*
import com.frolo.mediascan.MediaScanService
import com.frolo.muse.router.AppRouter
import com.frolo.muse.repository.AppearancePreferences
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.rx.disposeOnDestroyOf
import com.frolo.muse.sleeptimer.PlayerSleepTimer
import com.frolo.muse.startup.AppDebugController
import com.frolo.muse.ui.*
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.main.settings.journal.PlayerJournalDialog
import com.frolo.muse.ui.main.settings.library.duration.MinAudioFileDurationDialog
import com.frolo.muse.ui.main.settings.hidden.HiddenFilesDialog
import com.frolo.muse.ui.main.settings.info.AppInfoDialog
import com.frolo.muse.ui.main.settings.library.sections.LibrarySectionsDialog
import com.frolo.muse.ui.main.settings.library.filter.LibrarySongFilterDialog
import com.frolo.muse.ui.main.settings.libs.LicensesDialog
import com.frolo.muse.ui.main.settings.sleeptimer.SleepTimerDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsFragment : PreferenceFragmentCompat(),
        SleepTimerDialog.OnTimeSelectedListener,
        FragmentContentInsetsListener,
        ScrolledToTop {

    private val schedulerProvider: SchedulerProvider by lazy {
        activityComponent.provideSchedulerProvider()
    }
    private val preferences: Preferences by lazy {
        activityComponent.providePreferences()
    }
    private val appearancePreferences: AppearancePreferences by lazy {
        activityComponent.provideAppearancePreferences()
    }
    private val appRouter: AppRouter by lazy {
        activityComponent.provideAppRouter()
    }
    private val eventLogger: EventLogger by lazy {
        activityComponent.provideEventLogger()
    }
    private val appDebugController: AppDebugController by lazy {
        activityComponent.provideAppDebugController()
    }

    private val buyPremiumPreference: Preference? get() = findPreference("buy_premium")

    private val playbackFadingPreference: Preference? get() = findPreference("playback_fading")

    private val snowfallPreference: CheckBoxPreference? get() = findPreference("snowfall") as? CheckBoxPreference

    private val donatePreference: Preference? get() = findPreference("donate")

    private val ignoreBatteryOptimizationSettings: Preference? get() =
        findPreference("ignore_battery_optimization_settings")

    private val settingsViewModel: SettingsViewModel by lazy {
        val viewModelFactory = activityComponent.provideViewModelFactory()
        ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decorate(listView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeBillingViewModel(viewLifecycleOwner)
    }

    private fun decorate(list: androidx.recyclerview.widget.RecyclerView) {
        // Disabling over scroll because it looks not that good
        list.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.app_preferences)
        setupPreferences()
    }

    private fun setupPreferences() {

        ignoreBatteryOptimizationSettings?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                settingsViewModel.onIgnoreBatteryOptimizationSettingsClick()
                true
            }
        }

        buyPremiumPreference?.apply {
            // By default, it's invisible
            isVisible = false
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                settingsViewModel.onBuyPremiumPreferenceClicked()
                true
            }
        }

        playbackFadingPreference?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                settingsViewModel.onPlaybackFadingClick()
                true
            }
        }

        (findPreference("pause_playback") as CheckBoxPreference).apply {
            isChecked = preferences.shouldPauseOnUnplugged()
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                preferences.setPauseOnUnplugged(value as Boolean)
                true
            }
        }

        (findPreference("resume_playback") as CheckBoxPreference).apply {
            isChecked = preferences.shouldResumeOnPluggedIn()
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                preferences.setResumeOnPluggedIn(value as Boolean)
                true
            }
        }

        findPreference("library_sections").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showLibrarySectionChooser()
                true
            }
        }

        findPreference("library_song_filter").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showLibrarySongFilter()
                true
            }
        }

        snowfallPreference?.apply {
            appearancePreferences.isSnowfallEnabled()
                .firstOrError()
                .observeOn(schedulerProvider.main())
                .subscribe { isEnabled ->
                    this.isChecked = isEnabled
                    this.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                        if (value is Boolean) {
                            setSnowfallEnabled(value)
                        }
                        true
                    }
                }
                .disposeOnDestroyOf(this@SettingsFragment)
        }

        (findPreference("album_grid") as CheckBoxPreference).apply {
            isChecked = preferences.isAlbumGridEnabled
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                preferences.isAlbumGridEnabled = value as Boolean
                true
            }
        }

        findPreference("theme").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showThemeChooser()
                true
            }
        }

        findPreference("sleep_timer").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                processSleepTimer()
                true
            }
        }

        findPreference("hidden_files").apply {
            isVisible = Features.isPlainOldFileExplorerFeatureAvailable()
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showHiddenFilesDialog()
                true
            }
        }

        findPreference("exclude_short_songs").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showMinAudioFileDurationDialog()
                true
            }
        }

        findPreference("rescan_media_library").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showScanMediaDialog()
                true
            }
        }

        // About
        donatePreference?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showDonations()
                true
            }
        }

        findPreference("rate_this_app").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                eventLogger.logAppRatedFromSettings()
                context?.goToStore()
                true
            }
        }

        findPreference("share_this_app").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                eventLogger.logAppSharedFromSettings()
                context?.shareApp()
                true
            }
        }

        findPreference("licenses").apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showThirdPartyLibs()
                true
            }
        }

        findPreference("help_with_translations").apply {
            isVisible = false
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                activity?.helpWithTranslations()
                true
            }
        }

        findPreference("version").apply {
            summary = BuildConfig.VERSION_NAME
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showAppInfoDialog()
                true
            }
        }

        // Debug
        findPreference("debug").apply {
            isVisible = BuildConfig.DEBUG
        }

        findPreference("player_journal").apply {
            setOnPreferenceClickListener {
                showPlayerJournal()
                true
            }
        }

        findPreference("set_language").apply {
            setOnPreferenceClickListener {
                showLanguageChooser()
                true
            }
        }

        findPreference("consume_premium_product").apply {
            setOnPreferenceClickListener {
                settingsViewModel.onConsumePremiumProductClicked()
                true
            }
        }

        findPreference("reset_premium_trial").apply {
            setOnPreferenceClickListener {
                settingsViewModel.onResetPremiumTrialClicked()
                true
            }
        }

        findPreference("clear_user_data").apply {
            setOnPreferenceClickListener {
                appDebugController.clearUserData()
                Toast.makeText(requireContext(), "Cleared", Toast.LENGTH_SHORT).show()
                true
            }
        }

        findPreference("kill_process").apply {
            setOnPreferenceClickListener {
                appDebugController.killCompletely()
                true
            }
        }
    }

    private fun observeBillingViewModel(owner: LifecycleOwner) = with(settingsViewModel) {
        error.observeNonNull(owner) { err ->
            Toast.makeText(requireContext(), err.message.orEmpty(), Toast.LENGTH_SHORT).show()
        }

        isBuyPremiumOptionVisible.observe(owner) { visible ->
            buyPremiumPreference?.isVisible = visible == true
        }

        isDonateOptionVisible.observe(owner) { visible ->
            donatePreference?.isVisible = visible == true
        }

        snowfallOptionVisible.observe(owner) { visible ->
            snowfallPreference?.isVisible = visible == true
        }

        notifyPremiumProductConsumedEvent.observe(owner) {
            Toast.makeText(requireContext(), "Consumed", Toast.LENGTH_SHORT).show()
        }

        notifyPremiumTrialResetEvent.observe(owner) {
            Toast.makeText(requireContext(), "Reset", Toast.LENGTH_SHORT).show()
        }

        canIgnoreBatteryOptimizationSettings.observeNonNull(owner) { canIgnore ->
            ignoreBatteryOptimizationSettings?.isVisible = canIgnore
        }
    }

    override fun onTimeSelected(hours: Int, minutes: Int, seconds: Int) {
        val context = context ?: return
        if (PlayerSleepTimer.setAlarm(context, hours, minutes, seconds)) {
            eventLogger.logSleepTimerSet(hours = hours, minutes = minutes, seconds = seconds)
            activity?.let { Toast.makeText(it, R.string.sleep_timer_is_set, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun processSleepTimer() {
        val host = activity ?: return

        if (PlayerSleepTimer.isTimerSetUp(host)) {
            val l = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> showSleepTimer()
                    DialogInterface.BUTTON_NEUTRAL -> resetCurrentSleepTimer()
                }
            }
            AlertDialog.Builder(host)
                    .setMessage(R.string.you_have_already_set_up_sleep_timer)
                    .setPositiveButton(R.string.new_sleep_timer, l)
                    .setNeutralButton(R.string.reset_sleep_timer, l)
                    .show()
        } else {
            showSleepTimer()
        }
    }

    private fun showSleepTimer() {
        val sleepTimer = SleepTimerDialog.newInstance()
        sleepTimer.show(childFragmentManager, TAG_SLEEP_TIMER)
    }

    private fun resetCurrentSleepTimer() {
        val host = context ?: return
        if (PlayerSleepTimer.resetCurrentSleepTimer(host)) {
            Toast.makeText(context, R.string.sleep_timer_is_off, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHiddenFilesDialog() {
        val dialog = HiddenFilesDialog.newInstance()
        dialog.show(childFragmentManager, TAG_HIDDEN_FILES)
    }

    private fun showScanMediaDialog() {
        val activity = activity ?: return
        val l = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val context = context ?: return@OnClickListener
                    val permission = Manifest.permission.READ_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        // Permission already granted
                        startScanningMedia()
                    } else {
                        requestPermissions(arrayOf(permission), RC_SCAN_MEDIA)
                    }
                }
                DialogInterface.BUTTON_NEUTRAL -> Unit
                DialogInterface.BUTTON_NEGATIVE -> Unit
            }
        }
        MaterialAlertDialogBuilder(activity)
            .setIcon(R.drawable.ic_warning)
            .setTitle(R.string.rescan_media_library)
            .setMessage(R.string.do_you_want_to_rescan_media_library)
            .setPositiveButton(R.string.ok, l)
            .setNegativeButton(R.string.cancel, l)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_SCAN_MEDIA) {
            for (i in permissions.indices) {
                if (permissions[i] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // Permission granted
                        startScanningMedia()
                    }
                }
            }
        }
    }

    private fun startScanningMedia() {
        context?.let { safeContext ->
            MediaScanService.start(safeContext)
            eventLogger.logMediaLibraryScanned()
        }
    }

    private fun showThemeChooser() {
        appRouter.openThemeChooser()
    }

    private fun showMinAudioFileDurationDialog() {
        val dialog = MinAudioFileDurationDialog.newInstance()
        dialog.show(childFragmentManager, TAG_MIN_AUDIO_FILE_DURATION)
    }

    private fun showLibrarySectionChooser() {
        val dialog = LibrarySectionsDialog.newInstance()
        dialog.show(childFragmentManager, TAG_LIBRARY_SECTION_CHOOSER)
    }

    private fun showLibrarySongFilter() {
        val dialog = LibrarySongFilterDialog.newInstance()
        dialog.show(childFragmentManager, TAG_LIBRARY_SONG_FILTER)
    }

    private fun showThirdPartyLibs() {
        val dialog = LicensesDialog.newInstance()
        dialog.show(childFragmentManager, TAG_LICENCES)
    }

    private fun showDonations() {
        appRouter.openDonations()
    }

    private fun showAppInfoDialog() {
        val dialog = AppInfoDialog.newInstance()
        dialog.show(childFragmentManager, TAG_APP_INFO)
    }

    private fun showPlayerJournal() {
        val dialog = PlayerJournalDialog.newInstance()
        dialog.show(childFragmentManager, TAG_PLAYER_JOURNAL)
    }

    private fun showLanguageChooser() {
        val safeContext = this.context ?: return
        val languages: List<String> = listOf("Defined by system settings",
                "en", "de", "es", "fr", "hi", "ja", "ko", "pt", "ru", "tr", "uk", "zh")
        val currSavedLang: String? = preferences.language
        val checkedLangIndex: Int = if (currSavedLang.isNullOrBlank()) {
            0
        } else {
            languages.indexOf(currSavedLang)
        }
        MaterialAlertDialogBuilder(safeContext)
            .setTitle("Choose language")
            .setSingleChoiceItems(languages.toTypedArray(), checkedLangIndex) { dialog, index ->
                dialog?.dismiss()
                if (index > 0) {
                    val selectedLang: String = languages[index]
                    preferences.language = selectedLang
                } else {
                    preferences.language = null
                }
                activity?.recreate()
            }
            .show()
    }

    private fun setSnowfallEnabled(enabled: Boolean) {
        // Saving to the prefs
        appearancePreferences.setSnowfallEnabled(enabled)
            .doOnComplete {
                // Logging
                if (enabled) {
                    eventLogger.logSnowfallEnabled()
                } else {
                    eventLogger.logSnowfallDisabled()
                }
            }
            .doOnError { error ->
                eventLogger.log(error)
            }
            .observeOn(schedulerProvider.main())
            .subscribe()
            .disposeOnDestroyOf(this@SettingsFragment)
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            val recyclerView = listView

            // Try to set padding to the recycler view first, if there is one
            if (recyclerView != null) {
                recyclerView.setPadding(left, top, right, bottom)
                recyclerView.clipToPadding = false
            } else if (safeView is ViewGroup) {
                safeView.setPadding(left, top, right, bottom)
                safeView.clipToPadding = false
            }
        }
    }

    override fun scrollToTop() {
        view ?: return
        listView?.smoothScrollToTop()
    }

    companion object {
        private const val RC_SCAN_MEDIA = 1573

        private const val TAG_BUY_PREMIUM = "buy_premium"
        private const val TAG_LIBRARY_SECTION_CHOOSER = "library_section_chooser"
        private const val TAG_LIBRARY_SONG_FILTER = "library_song_filter"
        private const val TAG_THEME_CHOOSER = "theme_chooser"
        private const val TAG_LICENCES = "licences"
        private const val TAG_SLEEP_TIMER = "sleep_timer"
        private const val TAG_APP_INFO = "app_info"
        private const val TAG_HIDDEN_FILES = "hidden_files"
        private const val TAG_MIN_AUDIO_FILE_DURATION = "min_audio_file_duration"
        private const val TAG_PLAYBACK_FADING = "playback_fading"
        private const val TAG_PLAYER_JOURNAL = "player_journal"

        // Factory
        fun newInstance() = SettingsFragment()
    }

}
