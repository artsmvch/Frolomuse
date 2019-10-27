package com.frolo.muse.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class CharSequences {
    private CharSequences() { }

    private static final EmptyCharSequence EMPTY_CHAR_SEQUENCE = new EmptyCharSequence();

    private static class SingleCharSequence implements CharSequence {
        final char value;

        SingleCharSequence(char value) {
            this.value = value;
        }

        @Override
        public int length() {
            return 1;
        }

        @Override
        public char charAt(int index) {
            if (index == 0) return value;
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        @NonNull
        @Override
        public CharSequence subSequence(int beginIndex, int endIndex) {
            if (beginIndex < 0) {
                throw new IndexOutOfBoundsException(String.valueOf(beginIndex));
            }
            if (endIndex > 1) {
                throw new IndexOutOfBoundsException(String.valueOf(endIndex));
            }
            if (beginIndex > endIndex) {
                throw new IndexOutOfBoundsException(String.valueOf(endIndex - beginIndex));
            }
            if (beginIndex == 0 && endIndex == 1) {
                return this;
            }
            return EMPTY_CHAR_SEQUENCE;
        }

        @NotNull
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    private static final class EmptyCharSequence implements CharSequence {

        EmptyCharSequence() { }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public char charAt(int index) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        @NonNull
        @Override
        public CharSequence subSequence(int beginIndex, int endIndex) {
            if (beginIndex != 0 && endIndex != 0) {
                throw new IndexOutOfBoundsException(String.valueOf(endIndex - beginIndex));
            }
            return this;
        }

        @NotNull
        @Override
        public String toString() {
            return "";
        }
    }

    public static CharSequence single(char c) {
        return new SingleCharSequence(c);
    }

    public static CharSequence empty() {
        return EMPTY_CHAR_SEQUENCE;
    }

    public static CharSequence firstCharOrEmpty(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return EMPTY_CHAR_SEQUENCE;
        } else {
            return new SingleCharSequence(value.charAt(0));
        }
    }

    public static CharSequence firstNotSpaceCharOrEmpty(@Nullable String value) {
        if (value == null) {
            return EMPTY_CHAR_SEQUENCE;
        } else {
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (!Character.isSpaceChar(c)) {
                    return new SingleCharSequence(c);
                }
            }
            return EMPTY_CHAR_SEQUENCE;
        }
    }
}
