package com.frolo.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Test plugin is being applied...")
        target.tasks.register("testBuildSrc") {
            doLast {
                println("Testing BuildSrc...Done!")
            }
        }
        println("Test plugin has been applied")
    }
}