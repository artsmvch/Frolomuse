package com.frolo.plugin

import org.gradle.api.*
import org.gradle.api.internal.provider.ValueSupplier
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.options.Option
import java.io.PrintStream
import java.util.*

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
        printTaskWithDependencies(task, depth = 0, printStream = System.out)
    }
}

private fun printTaskWithDependencies(task: Task, depth: Int, printStream: PrintStream) {
    val indent = " ".repeat(depth)
    printStream.println(indent + task.name)
    task.dependsOn.forEach { dep ->
        collectTasksAsDep(task, dep).forEach { depTask ->
            printTaskWithDependencies(depTask, depth = depth + 1, printStream)
        }
    }
}

private fun collectTasksAsDep(
    task: Task,
    dep: Any,
): Collection<Task> {
    val collection: Collection<Task>? = when {
        dep is Task -> listOf(dep)
        dep is String ->
            listOf(task.project.tasks.getByPath(dep))
        dep is TaskProvider<*> ->
            listOf(dep.get())
        dep is ValueSupplier -> {
            val producerTasks = LinkedList<Task>()
            dep.producer.visitContentProducerTasks {
                producerTasks.add(this)
            }
            producerTasks
        }
        dep is Collection<*> -> {
            dep.filterNotNull().flatMap { collectTasksAsDep(task, it) }
        }
        // Must go last
        dep is Provider<*> && dep.isPresent -> {
            (dep.get() as? Task)?.let(Collections::singleton)
        }
        else -> null
    }
    return collection ?: emptyList<Task>()
}