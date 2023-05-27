package com.frolo.muse.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ExecutorQualifier(val value: ThreadType) {
    enum class ThreadType {
        MAIN,
        BACKGROUND
    }
}
