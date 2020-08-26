package com.frolo.muse.engine

import org.mockito.ArgumentMatcher


class AudioSourceQueueEquals<E>(private val wanted: E): ArgumentMatcher<E> {

    override fun matches(actual: E?): Boolean {
        val wanted = this.wanted

        if (wanted is AudioSourceQueue && actual is AudioSourceQueue) {
            val items1 = wanted.snapshot
            val items2 = actual.snapshot
            return wanted.type == actual.type &&
                    wanted.id == actual.id &&
                    wanted.name == actual.name &&
                    wanted.isUnique == actual.isUnique &&
                    items1.containsAll(items2) && items2.containsAll(items1)

        }

        return super.equals(actual)
    }

}