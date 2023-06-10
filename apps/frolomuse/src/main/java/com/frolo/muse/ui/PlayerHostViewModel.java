package com.frolo.muse.ui;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.frolo.logger.api.Logger;
import com.frolo.muse.player.PlayerHolder;
import com.frolo.muse.player.PlayerWrapper;
import com.frolo.muse.player.service.PlayerService;
import com.frolo.muse.logger.EventLogger;
import com.frolo.muse.ui.base.BaseAndroidViewModel;
import com.frolo.player.Player;
import com.frolo.threads.ThreadUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class PlayerHostViewModel extends BaseAndroidViewModel {
    private static final String LOG_TAG = "PlayerHostViewModel";

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            noteServiceConnected(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            noteServiceDisconnected();
        }
    };
    @Nullable
    private Disposable mPlayerObserver = null;

    private final MutableLiveData<Player> mPlayerLiveData = new MutableLiveData<Player>() {
        @Override
        protected void onActive() {
            // Why are we doing this here? Well, as of Android 12, there are restrictions regarding on
            // foreground services (more info here https://stackoverflow.com/a/70666991/9437681) causing
            // runtime exceptions when starting the PlayerService while the app is not in the foreground.
            // The idea is to start the PlayerService when this LiveData becomes active, because one
            // of its observers has transitioned to the started state.
            mMainHandler.post(mCallPlayerServiceCallback);
        }

        @Override
        protected void onInactive() {
            mMainHandler.removeCallbacks(mCallPlayerServiceCallback);
        }
    };
    private final PlayerWrapper mPlayerWrapper;
    private final MutableLiveData<Boolean> mDisconnectedLiveData =
            new MutableLiveData<Boolean>(false);

    private final AtomicBoolean mPlayerServiceCalled = new AtomicBoolean(false);

    private final Handler mMainHandler;
    private final Runnable mCallPlayerServiceCallback = this::callPlayerServiceIfNeeded;

    public PlayerHostViewModel(
            Application application,
            PlayerWrapper playerWrapper,
            EventLogger eventLogger) {
        super(application, eventLogger);
        mPlayerWrapper = playerWrapper;
        mPlayerLiveData.setValue(playerWrapper.getWrapped());
        mMainHandler = new Handler(application.getMainLooper());
    }

    private void callPlayerServiceIfNeeded() {
        if (mPlayerServiceCalled.getAndSet(true)) {
            return;
        }
        try {
            startPlayerService(getApplication());
            bindToPlayerService(getApplication());
        } catch (Throwable e) {
            // An attempt to fix this annoying crash
            // https://console.firebase.google.com/u/0/project/frolomuse/crashlytics/app/android:com.frolo.musp/issues/d55dbe8082c78becf73bcff49f0b1822
            Logger.e(LOG_TAG, e);
            mPlayerServiceCalled.set(false);
        }
    }

    private void startPlayerService(Application application) {
        PlayerService.start(application);
    }

    private void bindToPlayerService(Application application) {
        Intent intent = PlayerService.newIntent(application);
        int bindFlags = Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT;
        application.bindService(intent, mConnection, bindFlags);
    }

    private void unbindFromPlayerService() {
        Context appContext = getApplication();
        appContext.unbindService(mConnection);
    }

    @Nullable
    public final Player getPlayer() {
        return getPlayerLiveData().getValue();
    }

    @NonNull
    public final LiveData<Player> getPlayerLiveData() {
        return mPlayerLiveData;
    }

    @NonNull
    public final LiveData<Boolean> isDisconnectedLiveData() {
        return mDisconnectedLiveData;
    }

    private void noteServiceConnected(IBinder service) {
        if (service instanceof PlayerHolder) {
            disposePlayerObserver();
            mPlayerObserver = ((PlayerHolder) service).getPlayerAsync()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(playerInstance -> {
                    mPlayerWrapper.attachBase(playerInstance);
                    mPlayerLiveData.setValue(playerInstance);
                    onPlayerConnected(playerInstance);
                });
        }
    }

    private void disposePlayerObserver() {
        if (mPlayerObserver != null) {
            mPlayerObserver.dispose();
            mPlayerObserver = null;
        }
    }

    protected void onPlayerConnected(@NonNull Player player) {
    }

    private void noteServiceDisconnected() {
        disposePlayerObserver();
        Player player = mPlayerLiveData.getValue();
        if (player != null) {
            mPlayerLiveData.setValue(null);
            mPlayerWrapper.detachBase();
            mDisconnectedLiveData.setValue(true);
            onPlayerDisconnected(player);
        }
    }

    protected void onPlayerDisconnected(@NonNull Player player) {
    }

    @Override
    protected void onCleared() {
        mMainHandler.removeCallbacksAndMessages(null);
        if (mPlayerServiceCalled.get()) {
            unbindFromPlayerService();
        }
        disposePlayerObserver();
        // We delay detaching the player from the wrapper because all child
        // component view models (like fragments) are cleared after this one
        // and they may still need to use the player.
        // NOTE: If you are going to change this, make sure the component handles
        // recreation properly due to configuration changes and the out-of-memory killer.
        ThreadUtils.postOnMainThread(this::noteServiceDisconnected);
    }
}
