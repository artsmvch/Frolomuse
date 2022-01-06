package com.frolo.rx.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.annotations.NonNull;
import io.reactivex.internal.subscriptions.SubscriptionHelper;


final class FlowableIndexedValue<T> extends Flowable<IndexedValue<T>> {

    final Flowable<T> source;

    FlowableIndexedValue(@NonNull Flowable<T> source) {
        this.source = source;
    }

    @Override
    protected void subscribeActual(Subscriber<? super IndexedValue<T>> subscriber) {
        source.subscribe(new FlowableIndexedValueSubscriber<>(subscriber));
    }

    private static class FlowableIndexedValueSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super IndexedValue<T>> downstream;

        final AtomicInteger index = new AtomicInteger(0);

        Subscription upstream;

        FlowableIndexedValueSubscriber(Subscriber<? super IndexedValue<T>> subscriber) {
            this.downstream = subscriber;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T item) {
            IndexedValue<T> value = new IndexedValue<>(index.getAndIncrement(), item);
            downstream.onNext(value);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void request(long n) {
            upstream.request(n);
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }
    }

}
