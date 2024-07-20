package com.falsepattern.jtweaker

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class RemoveStubTask: DefaultTask() {
    @get:InputDirectory
    abstract val targetDirectory: DirectoryProperty

    init {
        group = "JTweaker"
        description = "Remove class stubs and remap references to them"
    }

    @TaskAction
    fun run() {
        Core.removeStub(targetDirectory)
    }
}