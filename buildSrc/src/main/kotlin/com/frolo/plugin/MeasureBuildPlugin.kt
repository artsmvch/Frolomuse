package com.frolo.plugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.*
import org.gradle.api.execution.TaskActionListener
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import java.io.File
import java.io.PrintStream

const val PROPERTY_MEASURE_BUILD = "measure_build"

class MeasureBuildPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (isMeasureBuildEnabled(target)) {
            listenToBuild(target)
        }
    }

    private fun isMeasureBuildEnabled(target: Project): Boolean {
        val propertyValue = target.findProperty(PROPERTY_MEASURE_BUILD)
        return propertyValue != null && propertyValue.toString() == "true"
    }

    private fun listenToBuild(target: Project) {
        val listenersImpl = ListenersImpl { reportBuild(target, it) }
        val gradle = target.gradle
        gradle.addProjectEvaluationListener(listenersImpl)
        gradle.projectsEvaluated {
            this.allprojects {
                gradle.addListener(listenersImpl)
            }
        }
    }

    private fun reportBuild(project: Project, info: BuildExecutionInfo) {
        // Print to the default output stream
        printImpl(info, System.out)
        // Print to a report file
        project.buildDir.also { buildDir ->
            val reportDir = File(buildDir, "report").apply {
                mkdirs()
            }
            val reportFile = File(reportDir, computeReportFilename()).apply {
                createNewFile()
            }
            val printStream = PrintStream(reportFile)
            printImpl(info, printStream)
        }
    }

    private fun computeReportFilename(): String {
        return "build_report_${System.currentTimeMillis()}.txt"
    }

    private fun printImpl(info: BuildExecutionInfo, stream: PrintStream) {
        stream.println("=====<<<<< Start build report >>>>>=====")

        stream.println(">> Settings")
        stream.println("Evaluated in ${info.settingsEvaluationInfo.duration} ms")

        stream.println(">> Projects")
        info.projectEvaluationInfoMap.forEach { (projectName, projectEvaluationInfo) ->
            println(projectName + " " + projectEvaluationInfo.duration + " ms")
        }

        stream.println(">> Tasks")
        info.taskExecutionInfoMap.forEach { (taskName, taskExecutionInfo) ->
            println(taskName + " " + taskExecutionInfo.duration + " ms " +
                    taskExecutionInfo.state?.taskExecutionOutcome)
        }

        stream.println("=====<<<<<< End build report >>>>>>=====")
    }

}

private class ListenersImpl(
    private val onBuildFinished: (BuildExecutionInfo) -> Unit
) : TaskExecutionListener, TaskActionListener, BuildListener, ProjectEvaluationListener {

    private val _settingsEvaluationInfo = ExecutionInfo(
        startTime = currentTimeMillis()
    )
    val settingsEvaluationInfo: ExecutionInfo get() = _settingsEvaluationInfo

    private val _projectEvaluationInfoMap = LinkedHashMap<String, ProjectEvaluationInfo>()
    val projectEvaluationInfoMap: Map<String, ProjectEvaluationInfo> get() = _projectEvaluationInfoMap

    private val _taskExecutionInfoMap = LinkedHashMap<String, TaskExecutionInfo>()
    val taskExecutionInfoMap: Map<String, TaskExecutionInfo> get() = _taskExecutionInfoMap

    private val Task.identifier: String get() = this.path
    private val Project.identifier: String get() = this.path

    private fun currentTimeMillis(): Long = System.currentTimeMillis()

    //region Tasks
    override fun beforeExecute(task: Task) {
        _taskExecutionInfoMap[task.identifier] = TaskExecutionInfo(
            name = task.identifier,
            startTime = currentTimeMillis()
        )
    }

    override fun afterExecute(task: Task, state: TaskState) {
        val info = _taskExecutionInfoMap[task.identifier]
        if (info != null) {
            info.endTime = currentTimeMillis()
            info.state = state
        } else {
            throw IllegalStateException("Task not found: ${task.identifier}")
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
        settings.gradle.addProjectEvaluationListener(this)
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
                projectEvaluationInfoMap = projectEvaluationInfoMap,
                taskExecutionInfoMap = taskExecutionInfoMap
            )
        )
    }
    //endregion

    //region Project evaluation
    override fun beforeEvaluate(project: Project) {
        onStartProjectEvaluation(project)
    }

    override fun afterEvaluate(project: Project, state: ProjectState) {
        onFinishProjectEvaluation(project, state)
    }
    //endregion

    private fun onStartProjectEvaluation(project: Project) {
        _projectEvaluationInfoMap[project.identifier] = ProjectEvaluationInfo(
            name = project.identifier,
            startTime = currentTimeMillis()
        )
    }

    private fun onFinishProjectEvaluation(project: Project, state: ProjectState) {
        _projectEvaluationInfoMap[project.identifier]?.also { info ->
            info.endTime = currentTimeMillis()
            info.state = state
        }
    }
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

private open class NamedExecutionInfo(
    val name: String,
    startTime: Long? = null,
    endTime: Long? = null
): ExecutionInfo(startTime, endTime)

private open class TaskExecutionInfo(
    name: String,
    startTime: Long? = null,
    endTime: Long? = null,
    var state: TaskState? = null
): NamedExecutionInfo(name, startTime, endTime)

private open class ProjectEvaluationInfo(
    name: String,
    startTime: Long? = null,
    endTime: Long? = null,
    var state: ProjectState? = null
): NamedExecutionInfo(name, startTime, endTime)

private class BuildExecutionInfo(
    val result: BuildResult,
    val settingsEvaluationInfo: ExecutionInfo,
    val projectEvaluationInfoMap: Map<String, ProjectEvaluationInfo>,
    val taskExecutionInfoMap: Map<String, TaskExecutionInfo>
)

