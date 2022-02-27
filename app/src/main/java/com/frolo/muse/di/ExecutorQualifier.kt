package com.frolo.muse.di

import javax.inject.Qualifier

@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ExecutorQualifier(val value: Type) {
    enum class Type {
        MAIN,
        QUERY
    }
}
