package com.falsepattern.jtweaker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.register

class JTweakerPlugin: Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        tasks.register<RemoveStubTask>("removeStub").configure {
            targetDirectory.set(layout.buildDirectory.dir("classes"))
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            dependsOn(JavaPlugin.TEST_CLASSES_TASK_NAME)
        }
    }
}