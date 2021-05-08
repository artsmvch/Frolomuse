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
import com.frolo.muse.rx.flowable.doOnFirst
import com.frolo.muse.rx.flowable.timeoutForFirstElement
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
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

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

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

        val source1 = getAlbumForPreview()
        val source2 = isPremiumPurchased()
        val combiner = BiFunction<Album, Boolean, Pair<Album, Boolean>> { album, isPremiumPurchased ->
            album to isPremiumPurchased
        }

        Flowable.combineLatest(source1, source2, combiner)
            .observeOn(schedulerProvider.main())
            .map { pair ->
                val album = pair.first
                val isPremiumPurchased = pair.second
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
            .map { themePages ->
                // Special sorting order for theme previews
                themePages.sortedBy { page ->
                    when (page.theme) {
                        Theme.LIGHT_BLUE -> 0
                        Theme.DARK_BLUE -> 100
                        Theme.DARK_FANCY -> 200
                        Theme.LIGHT_PINK -> 300
                        Theme.DARK_BLUE_ESPECIAL -> 400
                        Theme.DARK_PURPLE -> 500
                        Theme.DARK_ORANGE -> 600
                        Theme.DARK_GREEN -> 700
                    }
                }
            }
            .doOnSubscribe { _isLoading.value = true }
            .doOnFirst { _isLoading.value = false }
            .subscribeFor("load_themes") { themeItems ->
                onResult.invoke(themeItems)
            }
    }

    /**
     * Returns the best album model for the preview.
     */
    private fun getAlbumForPreview(): Flowable<Album> {
        // Fake album model
        val fakeAlbum = Album(0, "Test album", "Test artist", 0)
        val currAudioSource = player.getCurrent()
        // If there is an audio source being played by the player,
        // then we retrieve the album for this media item.
        val source = if (currAudioSource != null) {
            albumRepository.getItem(currAudioSource.albumId)
        } else {
            albumRepository.itemForPreview
        }
        return source
            .subscribeOn(schedulerProvider.worker())
            // 4 seconds should be enough to load an album for preview.
            .timeoutForFirstElement(4L, TimeUnit.SECONDS)
            .onErrorReturn { fakeAlbum }
    }

    /**
     * Returns true if the user is premium.
     */
    private fun isPremiumPurchased(): Flowable<Boolean> {
        // Check if the user has purchased the premium product.
        // The timeout for this check is 5 seconds, otherwise
        // we consider it not purchased.
        // In case of an error, we also consider it not purchased.
        return billingManager.isProductPurchased(productId = ProductId.PREMIUM, forceCheckFromApi = true)
            .timeout(5, TimeUnit.SECONDS, Flowable.just(false))
            .observeOn(schedulerProvider.main())
            .doOnError { err -> logError(err) }
            .onErrorReturnItem(false)
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