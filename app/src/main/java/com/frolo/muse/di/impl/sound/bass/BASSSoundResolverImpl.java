package com.frolo.muse.di.impl.sound.bass;

import android.util.Log;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.model.sound.Sound;
import com.frolo.muse.repository.SoundResolver;
import com.un4seen.bass.BASS;

import java.util.Arrays;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;


public class BASSSoundResolverImpl implements SoundResolver {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String LOG_TAG = "BASSSoundResolverImpl";

    private static int calcSoundCacheSize(int levelCount) {
        return 2 * 1024 * 1024; // 2 MB of memory
    }

    private static String getBASSErrorMessage(int errCode) {
        switch (errCode) {
            case BASS.BASS_OK: return "OK";
            case BASS.BASS_ERROR_MEM: return "BASS_ERROR_MEM";
            case BASS.BASS_ERROR_FILEOPEN: return "BASS_ERROR_FILEOPEN";
            case BASS.BASS_ERROR_DRIVER: return "BASS_ERROR_DRIVER";
            case BASS.BASS_ERROR_BUFLOST: return "BASS_ERROR_BUFLOST";
            case BASS.BASS_ERROR_HANDLE: return "BASS_ERROR_HANDLE";
            case BASS.BASS_ERROR_FORMAT: return "BASS_ERROR_FORMAT";
            case BASS.BASS_ERROR_POSITION: return "BASS_ERROR_POSITION";
            case BASS.BASS_ERROR_INIT: return "BASS_ERROR_INIT";
            case BASS.BASS_ERROR_START: return "BASS_ERROR_START";
            case BASS.BASS_ERROR_ALREADY: return "BASS_ERROR_ALREADY";
            case BASS.BASS_ERROR_NOCHAN: return "BASS_ERROR_NOCHAN";
            case BASS.BASS_ERROR_ILLTYPE: return "BASS_ERROR_ILLTYPE";
            case BASS.BASS_ERROR_ILLPARAM: return "BASS_ERROR_ILLPARAM";
            case BASS.BASS_ERROR_NO3D: return "BASS_ERROR_NO3D";
            case BASS.BASS_ERROR_NOEAX: return "BASS_ERROR_NOEAX";
            case BASS.BASS_ERROR_DEVICE: return "BASS_ERROR_DEVICE";
            case BASS.BASS_ERROR_NOPLAY: return "BASS_ERROR_NOPLAY";
            case BASS.BASS_ERROR_FREQ: return "BASS_ERROR_FREQ";
            case BASS.BASS_ERROR_NOTFILE: return "BASS_ERROR_NOTFILE";
            case BASS.BASS_ERROR_NOHW: return "BASS_ERROR_NOHW";
            case BASS.BASS_ERROR_EMPTY: return "BASS_ERROR_EMPTY";
            case BASS.BASS_ERROR_NONET: return "BASS_ERROR_NONET";
            case BASS.BASS_ERROR_CREATE: return "BASS_ERROR_CREATE";
            case BASS.BASS_ERROR_NOFX: return "BASS_ERROR_NOFX";
            case BASS.BASS_ERROR_NOTAVAIL: return "BASS_ERROR_NOTAVAIL";
            case BASS.BASS_ERROR_DECODE: return "BASS_ERROR_DECODE";
            case BASS.BASS_ERROR_DX: return "BASS_ERROR_DX";
            case BASS.BASS_ERROR_TIMEOUT: return "BASS_ERROR_FILEFORM";
            case BASS.BASS_ERROR_FILEFORM: return "BASS_ERROR_FILEFORM";
            case BASS.BASS_ERROR_SPEAKER: return "BASS_ERROR_SPEAKER";
            case BASS.BASS_ERROR_VERSION: return "BASS_ERROR_VERSION";
            case BASS.BASS_ERROR_CODEC: return "BASS_ERROR_CODEC";
            case BASS.BASS_ERROR_ENDED: return "BASS_ERROR_ENDED";
            case BASS.BASS_ERROR_BUSY: return "BASS_ERROR_BUSY";
            case BASS.BASS_ERROR_UNKNOWN: return "BASS_ERROR_UNKNOWN";

            case BASS.BASS_ERROR_JAVA_CLASS: return "BASS_ERROR_JAVA_CLASS";

            default: return null;
        }
    }

    private final int levelCount;

    private final SoundLruCache cache;

    public BASSSoundResolverImpl(int levelCount) {
        this.levelCount = levelCount;
        this.cache = new SoundLruCache(calcSoundCacheSize(levelCount));

        boolean initialized = BASS.BASS_Init(0, 44100, BASS.BASS_DEVICE_LATENCY);
        if (!initialized) {
            Log.e(LOG_TAG, "Failed to init BASS");
        }
    }

    @Override
    public Flowable<Sound> resolve(final String filepath) {
        return Flowable.fromCallable(new Callable<Sound>() {
            @Override
            public Sound call() throws Exception {
                // Checking the cache first
                final Sound cached = cache.get(filepath);
                if (cached != null)
                    return cached;

                // No cached value, creating a new one
                final Sound sound = resolve_Internal(filepath, levelCount);

                // Putting it in the cache for further optimization
                cache.put(filepath, sound);

                return sound;
            }
        });
    }

    private Sound resolve_Internal(String filename, int levelCount) throws Exception {
        if (levelCount < 0) {
            throw new IllegalArgumentException("Invalid level count: " + levelCount);
        }

        if (levelCount == 0) {
            int[] frameGains = new int[0];
            int maxFrameGain = 1;
            return new SoundImpl(frameGains, maxFrameGain);
        }

        final int chan = BASS.BASS_StreamCreateFile(filename, 0L, 0L, BASS.BASS_STREAM_DECODE | BASS.BASS_STREAM_PRESCAN);

        if (chan == 0) {
            throw new Exception("Failed to create stream for " + filename);
        }

        final long channelLength = BASS.BASS_ChannelGetLength(chan, BASS.BASS_3DMODE_NORMAL);
        long channelShiftBytes = channelLength / ((long) (levelCount + 1));

        final int[] levels = new int[levelCount];
        long bytePos = 0;

        int index = 0; // index of level
        int maxLevel = Integer.MIN_VALUE;
        do {
            boolean set = BASS.BASS_ChannelSetPosition(chan, bytePos, BASS.BASS_POS_BYTE);
            if (!set) {
                int errCode = BASS.BASS_ErrorGetCode();
                String errMsg = getBASSErrorMessage(errCode);
                if (DEBUG) Log.e(LOG_TAG, "Failed to set channel position to " + bytePos + ". Err code: " + errMsg);
            }

            int level = BASS.BASS_ChannelGetLevel(chan);

            {
                int errCode = BASS.BASS_ErrorGetCode();
                if (errCode != BASS.BASS_OK) {
                    String errMsg = getBASSErrorMessage(errCode);
                    if (DEBUG) Log.e(LOG_TAG, "Get channel level result: " + errMsg);
                }
            }

            levels[index++] = level;

            if (level > maxLevel) {
                maxLevel = level;
            }

            bytePos += channelShiftBytes;
        } while (index < levelCount);

        if (!areLevelsOK(levels)) {
            //throw new Exception("Failed to read levels");
            int[] newLevels = new int[levelCount];
            Arrays.fill(newLevels, 1);
            return new SoundImpl(newLevels, 10);
        }

        return new SoundImpl(levels, maxLevel);
    }

    private boolean areLevelsOK(int[] levels) {
        for (int level : levels) {
            if (level > 0) return true;
        }

        return false;
    }

}
