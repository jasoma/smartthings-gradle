package com.github.jasoma.stsync.gradle

import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

/**
 * Download the project script from the WebIDE and saves it locally.
 */
class DownloadScriptTask extends SmartThingsTask {

    DownloadScriptTask() {
        description = 'Download the app or device handler script from the WebIDE'
    }

    def getScriptFile() {
        def file = project.file(scriptFileName())
        return file
    }

    @TaskAction
    def download() {
        def ide = ext.getIde()
        String script = ext.project.downloadScript()

        def file = getScriptFile()
        file.createNewFile()
        file.write(script, StandardCharsets.UTF_8.name())
    }

}
