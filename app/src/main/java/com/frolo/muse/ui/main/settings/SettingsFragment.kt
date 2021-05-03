package com.frolo.muse.ui.main.settings

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.BuildConfig
import com.frolo.muse.Features
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.logger.*
import com.frolo.muse.mediascan.MediaScanService
import com.frolo.muse.model.Theme
import com.frolo.muse.repository.Preferences
import com.frolo.muse.sleeptimer.PlayerSleepTimer
import com.frolo.muse.ui.*
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.main.settings.premium.BuyPremiumDialog
import com.frolo.muse.ui.main.settings.journal.PlayerJournalDialog
import com.frolo.muse.ui.main.settings.playback.PlaybackFadingDialog
import com.frolo.muse.ui.main.settings.duration.MinAudioFileDurationDialog
import com.frolo.muse.ui.main.settings.hidden.HiddenFilesDialog
import com.frolo.muse.ui.main.settings.info.AppInfoDialog
import com.frolo.muse.ui.main.settings.library.LibrarySectionsDialog
import com.frolo.muse.ui.main.settings.libs.LicensesDialog
import com.frolo.muse.ui.main.settings.sleeptimer.SleepTimerDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsFragment : PreferenceFragmentCompat(),
        BuyPremiumDialog.OnBuyPremiumClickListener,
        SleepTimerDialog.OnTimeSelectedListener,
        NoClipping {

    private val preferences: Preferences by lazy {
        (requireContext().applicationContext as FrolomuseApp)
                .appComponent
                .providePreferences()
    }

    private val eventLogger: EventLogger by lazy {
        (requireContext().applicationContext as FrolomuseApp)
                .appComponent
                .provideEventLogger()
    }

    private val buyPremiumPreference: Preference? get() = findPreference("buy_premium")

    private val playbackFadingPreference: Preference? get() = findPreference("playback_fading")

    private val billingViewModel: BillingViewModel by lazy {
        val appComponent = requireContext().let { context ->
            FrolomuseApp.from(context).appComponent
        }
        val viewModelFactory = appComponent.provideVMFactory()
        ViewModelProviders.of(this, viewModelFactory)[BillingViewModel::class.java]
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

        buyPremiumPreference?.apply {
            // By default, it's invisible
            isVisible = false
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                billingViewModel.onBuyPremiumPreferenceClicked()
                true
            }
        }

        playbackFadingPreference?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showPlaybackFadingDialog()
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
    }

    private fun observeBillingViewModel(owner: LifecycleOwner) = with(billingViewModel) {
        isBuyPremiumOptionVisible.observe(owner) { visible ->
            buyPremiumPreference?.isVisible = visible == true
        }

        isPlaybackFadingProBadged.observeNonNull(owner) { isBadged ->
            val context = requireContext()
            playbackFadingPreference?.icon = AppCompatResources.getDrawable(context, R.drawable.pref_ic_fading_outline_28)?.let { drawable ->
                if (isBadged) ProBadgedDrawable(context, drawable) else drawable
            }
        }

        showPremiumBenefitsEvent.observe(owner) {
            showPremiumBenefitsDialog()
        }
    }

    override fun onBuyPremiumClick() {
        billingViewModel.onBuyPremiumClicked()
    }

    private fun showPremiumBenefitsDialog() {
        val dialog = BuyPremiumDialog.newInstance()
        dialog.show(childFragmentManager, TAG_BUY_PREMIUM)
    }

    private fun showPlaybackFadingDialog() {
        val dialog = PlaybackFadingDialog.newInstance()
        dialog.show(childFragmentManager, TAG_PLAYBACK_FADING)
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
        val activity = activity ?: return

        val currentTheme: Theme? = preferences.theme

        val icon = ContextCompat.getDrawable(activity, R.drawable.pref_ic_theme_outlined_28)

        // The order is really important
        val themeNames = arrayOf(
            getString(R.string.light_blue_theme),
            getString(R.string.light_pink_theme),
            getString(R.string.dark_blue_theme),
            getString(R.string.dark_especial_theme),
            getString(R.string.dark_purple_theme),
            getString(R.string.dark_orange_theme),
            getString(R.string.dark_green_theme)
        )

        val currentThemeIndex = when(currentTheme) {
            Theme.LIGHT_BLUE -> 0
            Theme.LIGHT_PINK -> 1
            Theme.DARK_BLUE -> 2
            Theme.DARK_BLUE_ESPECIAL -> 3
            Theme.DARK_PURPLE -> 4
            Theme.DARK_ORANGE -> 5
            Theme.DARK_GREEN -> 6
            else -> -1
        }

        MaterialAlertDialogBuilder(activity)
            .setTitle(getString(R.string.choose_theme))
            .setIcon(icon)
            .setSingleChoiceItems(themeNames, currentThemeIndex) { dialog, which ->
                dialog.dismiss()

                val selectedTheme: Theme? = when (which) {
                    0 -> Theme.LIGHT_BLUE
                    1 -> Theme.LIGHT_PINK
                    2 -> Theme.DARK_BLUE
                    3 -> Theme.DARK_BLUE_ESPECIAL
                    4 -> Theme.DARK_PURPLE
                    5 -> Theme.DARK_ORANGE
                    6 -> Theme.DARK_GREEN
                    else -> null // That's an error
                }

                if (selectedTheme != null && selectedTheme != currentTheme) {
                    preferences.saveTheme(selectedTheme)
                    eventLogger.logThemeChanged(selectedTheme)
                    if (activity is ThemeHandler) {
                        activity.handleThemeChange()
                    } else {
                        activity.recreate()
                    }
                }
            }
            .show()
    }

    private fun showMinAudioFileDurationDialog() {
        val dialog = MinAudioFileDurationDialog.newInstance()
        dialog.show(childFragmentManager, TAG_MIN_AUDIO_FILE_DURATION)
    }

    private fun showLibrarySectionChooser() {
        val dialog = LibrarySectionsDialog.newInstance()
        dialog.show(childFragmentManager, TAG_LIBRARY_SECTION_CHOOSER)
    }

    private fun showThirdPartyLibs() {
        val dialog = LicensesDialog.newInstance()
        dialog.show(childFragmentManager, TAG_LICENCES)
    }

    private fun showAppInfoDialog() {
        val dialog = AppInfoDialog.newInstance()
        dialog.show(childFragmentManager, TAG_APP_INFO)
    }

    private fun showPlayerJournal() {
        val dialog = PlayerJournalDialog.newInstance()
        dialog.show(childFragmentManager, TAG_PLAYER_JOURNAL)
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
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

    companion object {
        private const val RC_SCAN_MEDIA = 1573

        private const val TAG_BUY_PREMIUM = "buy_premium"
        private const val TAG_LIBRARY_SECTION_CHOOSER = "library_section_chooser"
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
