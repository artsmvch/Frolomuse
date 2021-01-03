package com.frolo.muse.rx.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.internal.subscriptions.SubscriptionHelper;

final class FlowableWithDefaultItemDelayed<T> extends Flowable<T> {

    final Flowable<T> source;
    final T defaultItem;
    final long delay;
    final TimeUnit unit;
    final Scheduler scheduler;

    FlowableWithDefaultItemDelayed(@NonNull Flowable<T> source, @NonNull T defaultItem, long delay, @NonNull TimeUnit unit, @NonNull Scheduler scheduler) {
        this.source = source;
        this.defaultItem = defaultItem;
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        Scheduler.Worker w = scheduler.createWorker();
        source.subscribe(new FlowableWithDefaultItemDelayedSubscriber<>(s, defaultItem, delay, unit, w));
    }

    private static class FlowableWithDefaultItemDelayedSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super T> downstream;
        final T defaultItem;
        final long delay;
        final TimeUnit unit;
        final Scheduler.Worker w;

        final AtomicBoolean emitted = new AtomicBoolean(false);

        Subscription upstream;

        FlowableWithDefaultItemDelayedSubscriber(Subscriber<? super T> downstream, T defaultItem, long delay, TimeUnit unit, Scheduler.Worker w) {
            this.downstream = downstream;
            this.defaultItem = defaultItem;
            this.delay = delay;
            this.unit = unit;
            this.w = w;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                w.schedule(new EmitDefaultItem(), delay, unit);
            }
        }

        @Override
        public void onNext(T t) {
            w.schedule(new OnNext(t));
        }

        @Override
        public void onError(Throwable t) {
            w.schedule(new OnError(t));
        }

        @Override
        public void onComplete() {
            w.schedule(new OnComplete());
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }

        @Override
        public void cancel() {
            upstream.cancel();
            w.dispose();
        }

        final class EmitDefaultItem implements Runnable {
            @Override
            public void run() {
                if (!emitted.get()) {
                    downstream.onNext(defaultItem);
                }
            }
        }

        final class OnNext implements Runnable {
            private final T t;

            OnNext(T t) {
                this.t = t;
            }

            @Override
            public void run() {
                emitted.set(true);
                downstream.onNext(t);
            }
        }

        final class OnError implements Runnable {
            private final Throwable t;

            OnError(Throwable t) {
                this.t = t;
            }

            @Override
            public void run() {
                try {
                    downstream.onError(t);
                } finally {
                    w.dispose();
                }
            }
        }

        final class OnComplete implements Runnable {
            @Override
            public void run() {
                try {
                    downstream.onComplete();
                } finally {
                    w.dispose();
                }
            }
        }
    }

}
