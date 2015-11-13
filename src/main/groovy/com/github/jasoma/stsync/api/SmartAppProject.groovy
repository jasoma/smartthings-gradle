package com.github.jasoma.stsync.api

import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.jsoup.nodes.Element

/**
 * Holds the details of a SmartApp project stored in the WebIDE.
 */
@ToString(includePackage = false, includeNames = true, cache = true)
@TupleConstructor
class SmartAppProject {

    private WebIDE ide

    String id
    String name
    String namespace
    String status
    String category

    @Delegate private AppResources resources

    /**
     * Scrape a row of from the 'My SmartApps' page list of apps for details of a project.
     *
     * @param tr a {code tr} element from the table body.
     * @param ide the ide used to load the project data.
     * @return the collected project data.
     */
    static def SmartAppProject fromRow(Element tr, WebIDE ide) {
        def id = tr.child(0).child(0).attr("href").split("/").last()
        def namePair = tr.child(1).text().split(":")
        def status = tr.child(2).text().trim()
        def category = tr.child(3).text().trim()
        def app = new SmartAppProject(ide: ide, id: id, namespace: namePair[0].trim(), name: namePair[1].trim(), status: status, category: category)
        app.validate()
        app.resources = ide.loadResources(app)
        return app
    }

    /**
     * Checks if all project properties have been set and are well-formed.
     */
    def void validate() {
        if (id == null || name == null || namespace == null || status == null || category == null) {
            throw new ApiException("Failed to load a complete data set for a SmartApp project, one or more properties was missing: $this")
        }
        if (id.isEmpty() || id.isAllWhitespace()) {
            throw new ApiException("'id' element is malformed or missing for a SmartAppProject: $this")
        }
    }

    /**
     * Downloads the groovy script file for this project from the WebIDE.
     *
     * @return the script contents.
     */
    def String downloadScript() {
        return ide.loadScript(this)
    }

    /**
     * Downloads the groocy script file for this project and writes it out. The destination write is
     * <strong>not</strong> closed by this operation.
     *
     * @param destination the writer to output the script with.
     */
    def void downloadScript(Writer destination) {
        destination.write(downloadScript())
        destination.flush()
    }

    /**
     * Uploads a new version of the script to the WebIDE.
     *
     * @param script the script contents to upload.
     */
    def void uploadScript(String script) {
        ide.uploadScript(this, script)
    }

    /**
     * Uploads a new version of the script from an external source to the WebIDE. The source reader is
     * <strong>not</strong> closed by this operation.
     *
     * @param source the source to read the script contents from.
     */
    def void uploadScript(Reader source) {
        uploadScript(source.readLines().join("\n"))
    }
}
