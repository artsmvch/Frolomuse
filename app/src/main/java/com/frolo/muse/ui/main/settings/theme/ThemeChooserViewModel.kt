package com.frolo.muse.ui.main.settings.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.billing.BillingManager
import com.frolo.muse.billing.ProductId
import com.frolo.muse.common.albumId
import com.frolo.muse.engine.Player
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.ProductOfferUiElementSource
import com.frolo.muse.logger.logProductOffered
import com.frolo.muse.logger.logThemeChanged
import com.frolo.muse.model.Theme
import com.frolo.muse.model.media.Album
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.repository.AlbumRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Single
import javax.inject.Inject


class ThemeChooserViewModel @Inject constructor(
    private val player: Player,
    private val albumRepository: AlbumRepository,
    private val preferences: Preferences,
    private val billingManager: BillingManager,
    private val navigator: Navigator,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _currentTheme by lazy {
        MutableLiveData<Theme>().apply {
            value = preferences.theme
        }
    }

    private val _themeItems by lazy {
        MutableLiveData<List<ThemePage>>().apply {
            loadThemes { items ->
                value = items
            }
        }
    }
    val themeItems: LiveData<List<ThemePage>> get() = _themeItems

    private val _applyThemeEvent = SingleLiveEvent<Theme>()
    val applyThemeEvent: LiveData<Theme> get() = _applyThemeEvent

    private fun loadThemes(onResult: (List<ThemePage>) -> Unit) {

        // Themes that are available by default
        val defaultThemes = listOf(
            Theme.LIGHT_BLUE,
            Theme.LIGHT_PINK,
            Theme.DARK_BLUE,
            Theme.DARK_BLUE_ESPECIAL,
            Theme.DARK_PURPLE,
            Theme.DARK_GREEN,
            Theme.DARK_ORANGE
        )

        val premiumThemes = listOf(
            Theme.DARK_FANCY
        )

        getAlbumForPreview()
            .observeOn(schedulerProvider.main())
            .flatMapPublisher { album ->
                billingManager.isProductPurchased(
                    productId = ProductId.PREMIUM,
                    forceCheckFromApi = true
                ).observeOn(schedulerProvider.main()).map { isPremiumPurchased ->
                    val currentTheme = preferences.theme

                    val premiumThemePages = premiumThemes.map { theme ->
                        ThemePage(
                            theme = theme,
                            isApplied = currentTheme == theme,
                            hasProBadge = !isPremiumPurchased,
                            album = album
                        )
                    }

                    val defaultThemePages = defaultThemes.map { theme ->
                        ThemePage(
                            theme = theme,
                            isApplied = currentTheme == theme,
                            hasProBadge = false,
                            album = album
                        )
                    }

                    premiumThemePages + defaultThemePages
                }
            }
            .observeOn(schedulerProvider.main())
            .subscribeFor("load_themes") { themeItems ->
                onResult.invoke(themeItems)
            }
    }

    /**
     * Returns the best album model for the preview.
     */
    private fun getAlbumForPreview(): Single<Album> {
        val currAudioSource = player.getCurrent()
        val source = if (currAudioSource != null) {
            albumRepository.getItem(currAudioSource.albumId)
        } else {
            albumRepository.itemForPreview
        }
        return source
            .firstOrError()
            .onErrorReturn {
                // Fake album model
                Album(0, "", "", 0)
            }
            .subscribeOn(schedulerProvider.worker())
    }

    fun onProBadgeClick(page: ThemePage) {
        if (!page.hasProBadge) {
            // wrong state
            return
        }

        eventLogger.logProductOffered(ProductId.PREMIUM, ProductOfferUiElementSource.THEME_PREVIEW_BADGE)
        navigator.offerToBuyPremium()
    }

    fun onApplyThemeClick(page: ThemePage) {
        val newTheme = page.theme
        if (_currentTheme.value == newTheme) {
            // No changes
            return
        }

        // Check if the user must be premium to apply this theme
        if (page.hasProBadge) {
            eventLogger.logProductOffered(ProductId.PREMIUM, ProductOfferUiElementSource.THEME_PREVIEW_APPLY)
            navigator.offerToBuyPremium()
            return
        }

        preferences.saveTheme(newTheme)
        eventLogger.logThemeChanged(newTheme)
        _applyThemeEvent.value = newTheme
        _currentTheme.value = newTheme
        _themeItems.value = themeItems.value?.map {
            it.copy(isApplied = it.theme == newTheme)
        }
    }

}