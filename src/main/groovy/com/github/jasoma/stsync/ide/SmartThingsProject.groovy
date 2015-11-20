package com.github.jasoma.stsync.ide

/**
 * Base class for all projects.
 */
abstract class SmartThingsProject {

    /**
     * The name of the project.
     */
    String name

    /**
     * The namespace of the project.
     */
    String namespace

    /**
     * Downloads the groovy script file for this project from the WebIDE.
     *
     * @return the script contents.
     */
    def abstract String downloadScript()

    /**
     * Downloads the groovy script file for this project and writes it out. The destination writer is
     * <strong>not</strong> closed by this operation.
     *
     * @param destination the writer to output the script with.
     */
    def abstract void downloadScript(Writer destination)

    /**
     * Uploads a new version of the script to the WebIDE.
     *
     * @param script the script contents to upload.
     */
    def abstract void uploadScript(String script)

    /**
     * Uploads a new version of the script from an external source to the WebIDE. The source reader is
     * <strong>not</strong> closed by this operation.
     *
     * @param source the source to read the script contents from.
     */
    def abstract void uploadScript(Reader source)
}
