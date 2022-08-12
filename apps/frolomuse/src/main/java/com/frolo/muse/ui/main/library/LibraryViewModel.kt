package com.frolo.muse.ui.main.library

import com.frolo.muse.logger.EventLogger
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class LibraryViewModel @Inject constructor(
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger)