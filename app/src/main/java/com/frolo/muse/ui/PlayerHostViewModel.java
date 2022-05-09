package com.frolo.muse.ui;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.frolo.muse.engine.PlayerHolder;
import com.frolo.muse.engine.PlayerWrapper;
import com.frolo.muse.engine.service.PlayerService;
import com.frolo.muse.logger.EventLogger;
import com.frolo.muse.ui.base.BaseAndroidViewModel;
import com.frolo.player.Player;
import com.frolo.threads.ThreadUtils;


public class PlayerHostViewModel extends BaseAndroidViewModel {

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

    private final MutableLiveData<Player> mPlayerLiveData = new MutableLiveData<>();
    private final PlayerWrapper mPlayerWrapper;
    private final MutableLiveData<Boolean> mDisconnectedLiveData =
            new MutableLiveData<Boolean>(false);

    public PlayerHostViewModel(
            Application application,
            PlayerWrapper playerWrapper,
            EventLogger eventLogger) {
        super(application, eventLogger);
        mPlayerWrapper = playerWrapper;
        mPlayerLiveData.setValue(playerWrapper.getWrapped());
        startPlayerService(application);
        bindToPlayerService(application);
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
    public final Player getPlayerWrapper() {
        return mPlayerWrapper;
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
            Player playerInstance = ((PlayerHolder) service).getPlayer();
            mPlayerLiveData.setValue(playerInstance);
            mPlayerWrapper.attachBase(playerInstance);
            onPlayerConnected(playerInstance);
        }
    }

    protected void onPlayerConnected(@NonNull Player player) {
    }

    private void noteServiceDisconnected() {
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
        unbindFromPlayerService();
        // We delay detaching the player from the wrapper because all child
        // component view models (like fragments) are cleared after this one
        // and they may still need to use the player.
        // NOTE: If you are going to change this, make sure the component handles
        // recreation properly due to configuration changes and the out-of-memory killer.
        ThreadUtils.postOnMainThread(this::noteServiceDisconnected);
    }
}
