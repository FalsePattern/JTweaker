package com.falsepattern.jtweaker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.register

class JTweakerPlugin: Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        tasks.register<RemoveStubTask>("removeStub").configure {
            targetDirectory.set(layout.buildDirectory.dir("classes"))
            mustRunAfter(JavaPlugin.CLASSES_TASK_NAME)
        }
        tasks.register<RemoveStubTask>("removeStubTests").configure {
            targetDirectory.set(layout.buildDirectory.dir("classes"))
            dependsOn(JavaPlugin.TEST_CLASSES_TASK_NAME)
        }
        afterEvaluate {
            tasks.named(JavaPlugin.CLASSES_TASK_NAME).configure {
                finalizedBy("removeStub")
            }
            try {
                tasks.named(JavaPlugin.TEST_CLASSES_TASK_NAME).configure {
                    finalizedBy("removeStubTests")
                }
            } catch (_: UnknownTaskException) {}
        }
    }
}