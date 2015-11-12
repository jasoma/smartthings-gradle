package com.github.jasoma.stsync.api
import groovy.transform.Immutable
import groovy.transform.ToString
import org.jsoup.nodes.Element

/**
 * Data class for holding the details of a SmartApp project stored in the WebIDE.
 */
@Immutable
@ToString(includePackage = false, includeNames = true, cache = true)
class SmartAppProject {

    String id
    String name
    String namespace
    String status
    String category

    /**
     * Scrape a row of from the 'My SmartApps' page list of apps for details of a project.
     *
     * @param tr a {code tr} element from the table body.
     * @return the collected project data.
     */
    static def SmartAppProject fromRow(Element tr) {
        def id = tr.child(0).child(0).attr("href").split("/").last()
        def namePair = tr.child(1).text().split(":")
        def status = tr.child(2).text().trim()
        def category = tr.child(3).text().trim()
        def app = new SmartAppProject(id: id, namespace: namePair[0].trim(), name: namePair[1].trim(), status: status, category: category)
        app.validate()
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
}
