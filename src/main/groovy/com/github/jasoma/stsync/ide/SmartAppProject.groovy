package com.github.jasoma.stsync.ide

import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.jsoup.nodes.Element

/**
 * Holds the details of a SmartApp project stored in the WebIDE.
 */
@ToString(includePackage = false, includeNames = true, cache = true)
@TupleConstructor
class SmartAppProject extends SmartThingsProject {

    private WebIDE ide

    String id
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
        app.resources = ide.downloadResourcesList(app)
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

    @Override
    def String downloadScript() {
        return ide.downloadScript(this)
    }

    @Override
    def void downloadScript(Writer destination) {
        destination.write(downloadScript())
        destination.flush()
    }

    @Override
    def void uploadScript(String script) {
        ide.uploadScript(this, script)
    }

    @Override
    def void uploadScript(Reader source) {
        uploadScript(source.readLines().join("\n"))
    }
}
