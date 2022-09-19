package com.frolo.plugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskActionListener
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class MeasureBuildPlugin : Plugin<Project> {
    private val listenerImpl = ListenersImpl(::reportBuild)

    override fun apply(target: Project) {
        val gradle = target.gradle
        // gradle.addListener(listenerImpl)
        gradle.projectsEvaluated {
            this.allprojects {
                gradle.addListener(listenerImpl)
            }
        }
    }

    private fun reportBuild(info: BuildExecutionInfo) {
        print(info)
    }

    private fun print(info: BuildExecutionInfo) {
        print("\n\n\n")
        println("=====<<<<< Start build report >>>>>=====")

        print("\n")
        println("<< Tasks >>")
        info.taskExecutionInfoMap.forEach { (taskName, taskExecutionInfo) ->
            println(taskName + ": " + taskExecutionInfo.duration + " ms")
        }

        print("\n")
        println("=====<<<<<< End build report >>>>>>=====")
        print("\n\n\n")
    }

}

private class ListenersImpl(
    private val onBuildFinished: (BuildExecutionInfo) -> Unit
) : TaskExecutionListener, TaskActionListener, BuildListener {

    private val _settingsEvaluationInfo = ExecutionInfo(
        startTime = currentTimeMillis()
    )
    val settingsEvaluationInfo: ExecutionInfo get() = _settingsEvaluationInfo

    private val _projectEvaluationInfoMap = LinkedHashMap<String, ExecutionInfo>()
    val projectEvaluationInfoMap: Map<String, ExecutionInfo> get() = _projectEvaluationInfoMap

    private val _taskExecutionInfoMap = LinkedHashMap<String, TaskExecutionInfo>()
    val taskExecutionInfoMap: Map<String, TaskExecutionInfo> get() = _taskExecutionInfoMap

    private fun currentTimeMillis(): Long = System.currentTimeMillis()

    //region Tasks
    override fun beforeExecute(task: Task) {
        _taskExecutionInfoMap[task.name] = TaskExecutionInfo(
            taskName = task.name,
            startTime = currentTimeMillis()
        )
    }

    override fun afterExecute(task: Task, state: TaskState) {
        val info = _taskExecutionInfoMap[task.name]
        if (info != null) {
            info.endTime = currentTimeMillis()
        } else {
            throw IllegalStateException("Task not found: ${task.name}")
        }
    }
    //endregion

    //region Actions
    override fun beforeActions(task: Task) {
        // No op
    }

    override fun afterActions(task: Task) {
        // No op
    }
    //endregion

    //region Build
    override fun settingsEvaluated(settings: Settings) {
        _settingsEvaluationInfo.endTime = currentTimeMillis()
    }

    override fun projectsLoaded(gradle: Gradle) {
        // No op
    }

    override fun projectsEvaluated(gradle: Gradle) {
        // No op
    }

    override fun buildFinished(result: BuildResult) {
        onBuildFinished.invoke(
            BuildExecutionInfo(
                result = result,
                settingsEvaluationInfo = settingsEvaluationInfo,
                taskExecutionInfoMap = taskExecutionInfoMap
            )
        )
    }
    //endregion
}

private open class ExecutionInfo(
    var startTime: Long? = null,
    var endTime: Long? = null
) {
    val duration: Long? get() {
        val startTime = this.startTime ?: return null
        val endTime = this.endTime ?: return null
        return endTime - startTime
    }
}

private class TaskExecutionInfo(
    val taskName: String,
    startTime: Long? = null,
    endTime: Long? = null
): ExecutionInfo(startTime, endTime)

private class BuildExecutionInfo(
    val result: BuildResult,
    val settingsEvaluationInfo: ExecutionInfo,
    val taskExecutionInfoMap: Map<String, TaskExecutionInfo>
)

