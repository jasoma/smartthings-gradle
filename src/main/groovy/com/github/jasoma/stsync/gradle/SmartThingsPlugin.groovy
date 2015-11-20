package com.github.jasoma.stsync.gradle
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin allowing for gradle controlled builds of SmartThings projects.
 */
class SmartThingsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create('smartthings', SmartThingsExtension)
        extension.readDefaults(project)

        def download = project.tasks.create('downloadScript', DownloadScriptTask)
        download.onlyIf { !download.getScriptFile().exists() }

        def forceDownload = project.tasks.create('forceDownload', DownloadScriptTask)
        forceDownload.description = 'Re-download the project script from the WebIDE replacing any existing local copy'

        def compile = project.tasks.create('compileLocal', LocalCompileTask)
        compile.dependsOn(download)

        def upload = project.tasks.create('uploadScript', UploadScriptTask)
        upload.dependsOn(compile)

        def diff = project.tasks.create('diffRemote', DiffTask)
        diff.onlyIf { project.file(diff.scriptFileName()).exists() }
    }

}
