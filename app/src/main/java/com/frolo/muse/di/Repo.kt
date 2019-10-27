package com.frolo.muse.di

import javax.inject.Qualifier

@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Repo(val value: Source = Source.LOCAL) {
    enum class Source {
        LOCAL, REMOTE, CACHE
    }
}
