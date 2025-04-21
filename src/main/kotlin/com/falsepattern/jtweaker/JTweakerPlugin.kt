package com.falsepattern.jtweaker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class JTweakerPlugin: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        tasks.withType<AbstractCompile> {
            val removalTask = tasks.register<RemoveStubTask>("${name}RemoveStubs")
            removalTask.configure {
                targets.set(this@withType.outputs.files)
                group = "jtweaker"
                dependsOn(this@withType)
            }
            finalizedBy(removalTask)
        }
    }
}