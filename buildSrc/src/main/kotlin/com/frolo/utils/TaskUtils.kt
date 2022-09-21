package com.frolo.plugin

import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.tasks.TaskState

internal val TaskState.taskExecutionOutcome: TaskExecutionOutcome? get() {
    return (this as? TaskStateInternal)?.outcome
}