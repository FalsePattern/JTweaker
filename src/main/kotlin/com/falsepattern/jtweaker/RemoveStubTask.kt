package com.falsepattern.jtweaker

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class RemoveStubTask: DefaultTask() {
    @get:InputFiles
    abstract val targets: Property<FileCollection>

    init {
        group = "JTweaker"
        description = "Remove class stubs and remap references to them"
    }



    @TaskAction
    fun run() {
        Core.removeStub(targets.get())
    }
}