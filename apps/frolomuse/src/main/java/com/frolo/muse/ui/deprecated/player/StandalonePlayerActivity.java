package com.frolo.muse.ui.deprecated.player;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.frolo.core.ui.glide.SquircleTransformation;
import com.frolo.logger.api.Logger;
import com.frolo.mediabutton.PlayButton;
import com.frolo.muse.BuildConfig;
import com.frolo.muse.R;
import com.frolo.muse.di.impl.permission.PermissionCheckerImpl;
import com.frolo.muse.sounder.Sounder;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;


@Deprecated
public class StandalonePlayerActivity extends AppCompatActivity {
    private static final String TAG = StandalonePlayerActivity.class.getSimpleName();

    private static final int READ_INTENT_URI = 31115;

    private static final String ARG_POSITION = "position";
    private static final String ARG_IS_PLAYING = "is_playing";

    private static final int FULLSCREEN_SYSTEM_UI_FLAGS = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    // UI views
    private View touchOutsideView;
    private View closeButton;
    private ImageView imageAlbumArt;
    private TextView textProgress;
    private PlayButton buttonPlay;
    private SeekBar seekBarProgress;
    private TextView textTitle;
    private TextView textArtist;

    private Disposable timerDisposable;
    private Disposable picDataDisposable;
    private boolean trackingBar = false;
    private Sounder sounder;
    // to be law abiding to audio manager
    private AudioManager audioManager;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (sounder != null)
                    sounder.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (sounder != null)
                    sounder.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            }
        }
    };

    private boolean requestAudioFocus() {
        int res = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        makeFullscreen();

        setContentView(R.layout.activity_player);

        touchOutsideView = findViewById(R.id.touch_outside);
        closeButton = findViewById(R.id.imv_close);
        imageAlbumArt = findViewById(R.id.imv_album_art);
        textProgress = findViewById(R.id.tv_progress);
        buttonPlay = findViewById(R.id.btn_play);
        seekBarProgress = findViewById(R.id.seek_bar_progress);
        textTitle = findViewById(R.id.tv_song_title);
        textArtist = findViewById(R.id.tv_song_artist);

        handleIntent(getIntent(), savedInstanceState);
        initUI();
        updateUI();
    }

    private void makeFullscreen() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(FULLSCREEN_SYSTEM_UI_FLAGS);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Sounder sounder = this.sounder;
        if (sounder != null) {
            int pos = sounder.getPos();
            boolean isPlaying = sounder.isPlaying();
            outState.putInt(ARG_POSITION, pos);
            outState.putBoolean(ARG_IS_PLAYING, isPlaying);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // save the last intent
        setIntent(intent);
        handleIntent(intent, null);
        initUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyResources();
    }

    private void destroyResources() {
        if (sounder != null) {
            sounder.release();
            sounder = null;
        }

        if (picDataDisposable != null) {
            picDataDisposable.dispose();
            picDataDisposable = null;
        }
    }

    private void handleIntent(Intent intent, @Nullable Bundle state) {
        final Uri uri = intent.getData();
        String action = intent.getAction();
        if (action != null && intent.getAction().equals(Intent.ACTION_VIEW) && uri != null) {
            final String permission = PermissionCheckerImpl.READ_AUDIO_PERMISSION;
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                // handle audio intent
                ContentResolver resolver = this.getContentResolver();
                try {
                    ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
                    if (pfd == null) {
                        finish();
                        return;
                    }

                    // Attempt to restore the sounder state
                    final int pos;
                    final boolean isPlaying;
                    if (state != null) {
                        pos = state.getInt(ARG_POSITION, 0);
                        isPlaying = state.getBoolean(ARG_IS_PLAYING, true);
                    } else {
                        pos = 0;
                        isPlaying = true;
                    }

                    // release previous sounder
                    if (sounder != null) sounder.release();

                    sounder = new Sounder(pfd.getFileDescriptor(), new Sounder.Callback() {
                        @Override public void onPlaybackChanged(Sounder sounder, boolean isPlaying) {
                            if (!isPlaying) audioManager.abandonAudioFocus(focusChangeListener);
                            updatePlayButtonState();
                            updateProgress();
                        }
                        @Override public void onCompletion(Sounder s) {
                            audioManager.abandonAudioFocus(focusChangeListener);
                            s.seekTo(0);
                            updatePlayButtonState();
                            updateProgress();
                        }
                        @Override public void onError(Sounder sounder, Throwable error) {
                            Logger.e(error);
                            if (BuildConfig.DEBUG) {
                                showErrorToast(error);
                            }
                        }
                    });

                    try {
                        pfd.close();
                    } catch (Throwable error) {
                        Logger.e(error);
                    }

                    sounder.seekTo(pos);
                    if (isPlaying) {
                        // start playing only if the audio manager allows
                        if (requestAudioFocus()) {
                            sounder.play();
                            //Intent i = new Intent(Const.ACTION_AUDIO_PLAYED);
                            //sendBroadcast(i);
                        }
                    }
                    updateUI();
                    updatePlayButtonState();
                } catch (Exception e) {
                    showErrorToast(e);
                    Logger.e(TAG, e);
                    finish();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[] {permission}, READ_INTENT_URI);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_INTENT_URI) {
            final String permission = PermissionCheckerImpl.READ_AUDIO_PERMISSION;
            for (int i = 0; i < permissions.length; i++) {
                String p = permissions[i];
                if (p.equals(permission)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        handleIntent(getIntent(), null);
                    } else {
                        // Permission not granted, no need to stay here
                        finish();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (timerDisposable != null) {
            timerDisposable.dispose();
        }
        updatePlayButtonState();
        updateProgress();
        timerDisposable = Observable.interval(1000L, TimeUnit.MILLISECONDS)
            .timeInterval()
            .observeOn(AndroidSchedulers.mainThread(), false, 100)
            .subscribe(new Consumer<Timed<Long>>() {
                @Override public void accept(Timed<Long> longTimed) throws Exception {
                    updateProgress();
                }
            });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (timerDisposable != null) {
            timerDisposable.dispose();
            timerDisposable = null;
        }

        if (isFinishing()) {
            destroyResources();
        }
    }

    private void initUI() {
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Sounder sounder = StandalonePlayerActivity.this.sounder;
                    if (sounder != null) {
                        textProgress.setText(getProgressText(progress, sounder.getDuration()));
                    }
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {
                trackingBar = true;
            }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                Sounder sounder = StandalonePlayerActivity.this.sounder;
                if (sounder != null) {
                    int position = seekBar.getProgress();
                    sounder.seekTo(position);
                }
                trackingBar = false;
            }
        });
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Sounder sounder = StandalonePlayerActivity.this.sounder;
                if (sounder != null) {
                    if (!sounder.isPlaying()) {
                        if (requestAudioFocus()) {
                            sounder.toggle();
                        }
                    } else {
                        sounder.toggle();
                    }
                }
            }
        });
    }

    private void updateUI() {
        try {
            Uri arg = getIntent().getData();
            if (arg != null) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(this, arg);
                String songTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artistTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                byte[] picData = mmr.getEmbeddedPicture();
                loadAlbumArt(picData);
                mmr.release();
                if (songTitle != null && !songTitle.isEmpty()) {
                    textTitle.setText(songTitle);
                } else {
                    textTitle.setText(R.string.placeholder_unknown);
                }
                if (artistTitle != null && !artistTitle.isEmpty()) {
                    textArtist.setText(artistTitle);
                } else {
                    textArtist.setText(R.string.placeholder_unknown);
                }
            }
        } catch (Exception e) {
            Logger.e(e);
            textTitle.setText(R.string.placeholder_unknown);
            textArtist.setText(R.string.placeholder_unknown);
        }

        if (sounder != null) {
            seekBarProgress.setMax(sounder.getDuration());
            textProgress.setText(getProgressText(sounder.getPos(), sounder.getDuration()));
        }
    }

    private void updatePlayButtonState() {
        Sounder sounder = this.sounder;
        if (sounder != null) {
            PlayButton.State state = sounder.isPlaying() ?
                    PlayButton.State.PAUSE : PlayButton.State.RESUME;
            buttonPlay.setState(state, true);
        }
    }

    private void updateProgress() {
        Sounder sounder = this.sounder;
        if (sounder != null) {
            if (!trackingBar) {
                seekBarProgress.setProgress(sounder.getPos());
                textProgress.setText(getProgressText(sounder.getPos(), sounder.getDuration()));
            }
        }
    }

    private void loadAlbumArt(@Nullable final byte[] picData) {
        if (picData != null) {
            Single.fromCallable(new Callable<Bitmap>() {
                @Override public Bitmap call() throws Exception {
                    return BitmapFactory.decodeByteArray(picData, 0, picData.length);
                }
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (picDataDisposable != null) {
                            picDataDisposable.dispose();
                        }
                        picDataDisposable = d;
                    }

                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Glide.with(StandalonePlayerActivity.this)
                            .load(bitmap)
                            .transform(new SquircleTransformation(2.9))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .error(Glide.with(StandalonePlayerActivity.this).load(R.drawable.ic_album_grey_72dp))
                            .into(imageAlbumArt);
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadDefaultAlbumArt();
                    }
                });
        } else {
            loadDefaultAlbumArt();
        }
    }

    private void loadDefaultAlbumArt() {
        Glide.with(this)
            .load(R.drawable.ic_album_grey_72dp)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageAlbumArt);
    }

    private void showErrorToast(Throwable error) {
        String text = error.getMessage();
        if (text == null || text.isEmpty()) {
            text = "Error";
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private String convertDurationToString(int position) {
        int totalSeconds = position / 1000;
        int totalMinutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        //int totalHours = totalMinutes / 60;
        String format = (seconds < 10) ? "%d:0%d" : "%d:%d";
        return String.format(format, totalMinutes, seconds);
    }

    private String getProgressText(int position, int duration) {
        return convertDurationToString(position) + " / " + convertDurationToString(duration);
    }
}
