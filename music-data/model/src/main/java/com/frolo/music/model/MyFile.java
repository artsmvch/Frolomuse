package com.frolo.music.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.Serializable;


@Deprecated
public class MyFile implements Media, Serializable {
    private /*non-null*/ final File file;
    private final boolean isSongFile;
    private final MediaId mediaId;

    public MyFile(File javaFile, boolean isSongFile) {
        if (javaFile == null) {
            throw new IllegalArgumentException("JavaFile is null");
        }
        this.file = javaFile;
        this.isSongFile = isSongFile;
        this.mediaId = MediaId.createLocal(Media.MY_FILE, file.hashCode());
    }

    public @NonNull File getJavaFile() {
        return file;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isSongFile() {
        return isSongFile;
    }

    public @Nullable MyFile getParent() {
        final File parentJavaFile = file.getParentFile();
        return parentJavaFile != null ? new MyFile(parentJavaFile, false) : null;
    }

    @Override
    public MediaId getMediaId() {
        return mediaId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj != null && (obj instanceof MyFile)) {
            MyFile another = (MyFile) obj;
            return isSongFile == another.isSongFile && file.equals(another.file);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return mediaId.hashCode();
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }
}
