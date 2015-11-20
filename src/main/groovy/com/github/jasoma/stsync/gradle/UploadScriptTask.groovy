package com.github.jasoma.stsync.gradle

import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

/**
 * Uploads the local script file to the WebIDE.
 */
class UploadScriptTask extends SmartThingsTask {

    UploadScriptTask() {
        description = 'Upload the local project script to the WebIDE'
    }

    @InputFile
    def getScriptFile() {
        return project.file(scriptFileName())
    }

    @TaskAction
    def upload() {
        def project = ext.project
        def scriptReader = scriptFile.newReader(StandardCharsets.UTF_8.name())
        project.uploadScript(scriptReader)
    }

}
