package com.frolo.muse.sounder;

import android.media.MediaPlayer;

import java.io.FileDescriptor;


/**
 * Simple audio engine that allows to play one audio file.
 * Supports such default player features as:
 * - {@link #play};
 * - {@link #pause};
 * - {@link #resume};
 * - {@link #toggle};
 * - {@link #isPlaying()};
 * - {@link #release()}
 * - {@link #getPos()};
 * - {@link #seekTo(int)};
 * - {@link #getDuration()};
 *
 * The state of the Sounder may be observed with {@link Callback}.
 *
 * The Sounder is backed with {@link MediaPlayer} engine.
 */
public final class Sounder {
    private final MediaPlayer mEngine;
    private final Callback mCallback;

    public interface Callback {
        void onPlaybackChanged(Sounder sounder, boolean isPlaying);
        void onCompletion(Sounder sounder);
        void onError(Sounder sounder, Throwable error);
    }

    public Sounder(FileDescriptor fd, final Callback callback) throws Exception {
        mEngine = new MediaPlayer();
        mEngine.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mp) {
                Sounder s = Sounder.this;
                s.mCallback.onCompletion(s);
            }
        });
        mCallback = callback;

        mEngine.setDataSource(fd);
        mEngine.prepare();
    }

    public void play() {
        try {
            mEngine.start();
            mCallback.onPlaybackChanged(this, true);
        } catch (Exception e) {
            mCallback.onError(this, e);
        }
    }

    public void pause() {
        try {
            mEngine.pause();
            mCallback.onPlaybackChanged(this, true);
        } catch (Exception e) {
            mCallback.onError(this, e);
        }
    }

    public void resume() {
        try {
            mEngine.start();
            mCallback.onPlaybackChanged(this, true);
        } catch (Exception e) {
            mCallback.onError(this, e);
        }
    }

    public void toggle() {
        try {
            if (mEngine.isPlaying())
                mEngine.pause();
            else mEngine.start();

            mCallback.onPlaybackChanged(this, mEngine.isPlaying());
        } catch (Exception e) {
            mCallback.onError(this, e);
        }
    }

    public boolean isPlaying() {
        try {
            return mEngine.isPlaying();
        } catch (Exception e) {
            mCallback.onError(this, e);
            return false;
        }
    }

    public void release() {
        try {
            mEngine.reset();
            mEngine.release();
            mCallback.onPlaybackChanged(this, false);
        } catch (Exception e) {
            mCallback.onError(this, e);
        }
    }

    public int getPos() {
        try {
            return mEngine.getCurrentPosition();
        } catch (Exception e) {
            mCallback.onError(this, e);
            return 0;
        }
    }

    public int getDuration() {
        try {
            return mEngine.getDuration();
        } catch (Exception e) {
            mCallback.onError(this, e);
            return 1;
        }
    }

    public void seekTo(int pos) {
        try {
            mEngine.seekTo(pos);
        } catch (Exception e) {
            mCallback.onError(this, e);
        }
    }
}
