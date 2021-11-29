package com.frolo.muse.rx.flowable;

import java.util.Objects;


public final class IndexedValue<T> {
    private final int index;
    private final T value;

    public IndexedValue(int index, T value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexedValue<?> that = (IndexedValue<?>) o;
        return index == that.index &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, value);
    }

    @Override
    public String toString() {
        return "IndexedValue{" +
                "index=" + index +
                ", value=" + value +
                '}';
    }
}
