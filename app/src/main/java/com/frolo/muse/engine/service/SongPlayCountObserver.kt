package com.frolo.muse.engine.service

import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.interactor.media.DispatchSongPlayedUseCase
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable


class SongPlayCountObserver constructor(
        private val schedulerProvider: SchedulerProvider,
        private val dispatchSongPlayedUseCase: DispatchSongPlayedUseCase
): SimplePlayerObserver() {

    private val disposables = CompositeDisposable()

    override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) {
        song?.also { safeSong ->
            dispatchSongPlayedUseCase.dispatchSongPlayed(safeSong)
                    .observeOn(schedulerProvider.main())
                    .subscribe({ }, { })
                    .also { d -> disposables.add(d) }
        }
    }

    override fun onShutdown(player: Player) {
        disposables.clear()
    }

}