package com.falsepattern.jtweaker;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JTweakerPlugin implements Plugin<Project> {
    public void apply(final Project project) {
        project.task("removeStub", (task) -> {
            task.setGroup("JTweaker");
            task.setDescription("Remove class stubs and remap references to them");
            task.dependsOn("classes");
            task.doLast((task1) -> Core.removeStub(project));
        });
    }
}
