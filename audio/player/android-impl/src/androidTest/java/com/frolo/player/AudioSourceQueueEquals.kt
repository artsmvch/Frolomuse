package com.frolo.player

import org.mockito.ArgumentMatcher


class AudioSourceQueueEquals<E>(private val wanted: E): ArgumentMatcher<E> {

    override fun matches(actual: E?): Boolean {
        val wanted = this.wanted

        if (wanted is AudioSourceQueue && actual is AudioSourceQueue) {
            return wanted.deepEquals(actual)
        }

        return super.equals(actual)
    }

}