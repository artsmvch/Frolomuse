package com.frolo.muse.util;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Set;

// Custom collection that holds only 1 item and implements all known collection interfaces
final class SingleItemCollection<E> implements Collection<E>,
        List<E>,
        Set<E>,
        Queue<E>,
        RandomAccess,
        Serializable {

    private final E item;

    SingleItemCollection(E item) {
        this.item = item;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return item == o || (item != null && o != null && item.equals(o));
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new SingleItemIterator<>(item);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        Object[] array = new Object[1];
        array[0] = item;
        return array;
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E poll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E element() {
        return item;
    }

    @Override
    public E peek() {
        return item;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        Iterator<?> it = c.iterator();
        boolean f = it.hasNext() && contains(it.next());
        if (it.hasNext()) f = false;
        return f;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
        if (index == 0) return item;
        throw new IndexOutOfBoundsException(String.valueOf(index));
    }

    @Override
    public E set(int index, E element) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
    }

    @Override
    public void add(int index, E element) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
    }

    @Override
    public E remove(int index) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
    }

    @Override
    public int indexOf(Object o) {
        if (item == o || (item != null && o != null && item.equals(o)))
            return 0;
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator() {
        return new SingleItemListIterator<>(item);
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int index) {
        if (index != 0)
            throw new IndexOutOfBoundsException(String.valueOf(index));
        return listIterator();
    }

    @NonNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        if (fromIndex != 0 || toIndex != 0)
            throw new IndexOutOfBoundsException();
        return new SingleItemCollection<>(item);
    }

    private static class SingleItemIterator<E> implements Iterator<E> {
        E item;
        boolean retrieved = false;
        SingleItemIterator(E item) {
            this.item = item;
        }
        @Override
        public boolean hasNext() {
            return !retrieved;
        }
        @Override
        public E next() {
            if (retrieved)
                throw new NoSuchElementException();
            retrieved = true;
            return item;
        }
    }

    private static class SingleItemListIterator<E> extends SingleItemIterator<E>
            implements ListIterator<E> {
        SingleItemListIterator(E item) {
            super(item);
        }

        @Override
        public boolean hasPrevious() {
            return retrieved;
        }

        @Override
        public E previous() {
            if (!retrieved)
                throw new NoSuchElementException();
            retrieved = false;
            return item;
        }

        @Override
        public int nextIndex() {
            return retrieved ? 1 : 0;
        }

        @Override
        public int previousIndex() {
            return retrieved ? -1 : 0;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException();
        }
    }
}
