package com.github.jasoma.stsync.gradle

import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

/**
 * Compiles the local project script. Does not validate any of the SmartThings API usage, only validates that the
 * script is valid groovy code.
 */
class LocalCompileTask extends SmartThingsTask {

    LocalCompileTask() {
        description = 'Compiles the local script file to check it is valid groovy code'
    }

    @InputFile
    def getScriptFile() {
        return project.file(scriptFileName())
    }

    @TaskAction
    def compile() {
        def compiler = new GroovyShell()
        def script = compiler.parse(getScriptFile())
    }

}
