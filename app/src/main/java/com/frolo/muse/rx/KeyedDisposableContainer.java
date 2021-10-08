package com.frolo.muse.rx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableContainer;


public final class KeyedDisposableContainer<K> implements Disposable, DisposableContainer {

    static final Object NULL_KEY = null;

    private final Object mLock = new Object();

    private final CompositeDisposable mContainer = new CompositeDisposable();

    // Guarded by mLock
    private final Map<Object, Disposable> mKeyedContainer = new HashMap<>();

    @Override
    public void dispose() {
        synchronized (mLock) {
            mContainer.dispose();
            mKeyedContainer.clear();
        }
    }

    @Override
    public boolean isDisposed() {
        return mContainer.isDisposed();
    }

    @Override
    public boolean add(Disposable disposable) {
        Disposable old = addInternal(NULL_KEY, disposable);
        if (old != null) {
            old.dispose();
        }
        return old != null;
    }

    @Override
    public boolean remove(Disposable disposable) {
        return removeInternal(disposable, false);
    }

    @Override
    public boolean delete(Disposable disposable) {
        return removeInternal(disposable, true);
    }

    @Nullable
    public Disposable add(K key, Disposable disposable) {
        return addInternal(key, disposable);
    }

    @Nullable
    private Disposable addInternal(Object key, Disposable disposable) {
        Disposable old;
        synchronized (mLock) {
            old = mKeyedContainer.put(key, disposable);
            if (old != null) {
                mContainer.delete(old);
            }
            mContainer.add(disposable);
        }
        return old;
    }

    @Nullable
    public Disposable remove(K key) {
        return removeInternal(key, false);
    }

    @Nullable
    public Disposable delete(K key) {
        return removeInternal(key, true);
    }

    private Disposable removeInternal(K key, boolean delete) {
        final Disposable disposable;
        synchronized (mLock) {
            disposable = mKeyedContainer.remove(key);
            if (disposable != null) {
                if (delete) {
                    mContainer.delete(disposable);
                } else {
                    mContainer.remove(disposable);
                }
            }
        }
        return disposable;
    }

    private boolean removeInternal(Disposable disposable, boolean delete) {
        final boolean result;
        synchronized (mLock) {
            // First, remove all related keys from the map
            Iterator<Map.Entry<Object, Disposable>> it = mKeyedContainer.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, Disposable> entry = it.next();
                if (Objects.equals(entry.getValue(), disposable)) {
                    it.remove();
                }
            }

            if (delete) {
                result = mContainer.delete(disposable);
            } else {
                result = mContainer.remove(disposable);
            }
        }
        return result;
    }

    boolean containsKey(K key) {
        synchronized (mLock) {
            return mKeyedContainer.containsKey(key);
        }
    }

    boolean containsValue(Disposable disposable) {
        synchronized (mLock) {
            return mKeyedContainer.containsValue(disposable);
        }
    }
}
