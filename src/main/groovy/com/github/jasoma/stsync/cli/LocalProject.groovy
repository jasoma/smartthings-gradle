package com.github.jasoma.stsync.cli

import groovy.transform.TupleConstructor

/**
 * Data class holding references to the files needed to create a local project.
 */
@TupleConstructor
class LocalProject {

    File dir
    File projectScript
    File buildScript

    /**
     * Sets up the local project directory and ensures the script files can be created/
     *
     * @param name the name of the project.
     * @return the local project instance holding the file references.
     * @throws IOException if any of the files or directories cannot be created.
     */
    def static setup(String name) throws IOException {
        def projectDir = new File(name)
        if (projectDir.exists()) {
            throw new IOException("Path ${projectDir.absolutePath} already exists")
        }
        if (!projectDir.mkdir()) {
            throw new IOException("Could not create project directory ${projectDir.absolutePath}")
        }

        def projectScript = new File(projectDir, "${name}.groovy")
        if (!projectScript.createNewFile()) {
            throw new IOException("Could not create the project script file")
        }

        def buildScript = new File(projectDir, "build.gradle")
        if (!buildScript.createNewFile()) {
            throw new IOException("Could not create the build script")
        }

        return new LocalProject(dir: projectDir, projectScript: projectScript, buildScript: buildScript)
    }

}
