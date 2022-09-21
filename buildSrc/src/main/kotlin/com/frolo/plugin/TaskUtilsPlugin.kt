package com.frolo.plugin

import org.gradle.api.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class TaskUtilsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val clazz: Class<out Task> = PrintTaskDependenciesTask::class.java
        target.tasks.register("printTaskDependencies", clazz)
    }
}

internal abstract class PrintTaskDependenciesTask : DefaultTask(), Task {
    @get:Input
    abstract val taskPath: Property<String>

    @Option(
        option="task-path",
        description = "Task for which to print dependencies.")
    fun setTaskPathOption(value: String) = taskPath.set(value)

    @TaskAction
    fun printTaskDependencies() {
        val taskPath = this.taskPath.get()
            ?: throw GradleException("Task path is not specified")
        val task = project.tasks.findByPath(taskPath)
            ?: throw GradleException("Task $taskPath not found")
        printTaskWithDependencies(task, 0)
    }

    private fun printTaskWithDependencies(task: Task, depth: Int) {
        val tabs = "\t".repeat(depth)
        println(tabs + task.path)
        task.dependsOn.forEach { dependency ->
            if (dependency is Task) {
                printTaskWithDependencies(dependency, depth + 1)
            }
        }
    }
}