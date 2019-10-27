package com.frolo.muse

import org.mockito.stubbing.OngoingStubbing


/*mocking void methods*/
fun <T> OngoingStubbing<T>.thenDoNothing(): OngoingStubbing<T> {
    return then {  }
}