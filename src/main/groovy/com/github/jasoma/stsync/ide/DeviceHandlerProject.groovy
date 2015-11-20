package com.github.jasoma.stsync.ide

import org.jsoup.nodes.Element

/**
 * Holds the details of a DeviceHandler/DeviceType project stored in the WebIDE.
 */
class DeviceHandlerProject extends SmartThingsProject {

    private WebIDE ide

    String id
    String status
    List<String> capabilities
    boolean oauthEnabled

    /**
     * Scrape a row of from the 'My Device Types' page list of devices for details of a project.
     *
     * @param tr a {code tr} element from the table body.
     * @param ide the ide used to load the project data.
     * @return the collected project data.
     */
    def static DeviceHandlerProject fromRow(Element tr, WebIDE ide) {
        def id = tr.child(0).child(0).attr("href").split("/").last()
        def namePair = tr.child(1).text().split(":")
        def status = tr.child(2).text().trim()
        def capabilities = tr.child(3).text().split(",").collect { it.trim() }
        def oath = tr.child(4).text().trim().toBoolean()
        def device = new DeviceHandlerProject(
                ide: ide,
                id: id,
                namespace: namePair[0].trim(),
                name: namePair[1].trim(),
                status: status,
                capabilities: capabilities,
                oauthEnabled: oath)
        device.validate()
        return device
    }

    /**
     * Checks if all project properties have been set and are well-formed.
     */
    def void validate() {
        if (id == null || name == null || namespace == null || status == null || capabilities == null) {
            throw new ApiException("Failed to load a complete data set for a DeviceHandlerProject, one or more properties was missing: $this")
        }
        if (id.isEmpty() || id.isAllWhitespace()) {
            throw new ApiException("'id' element is malformed or missing for a DeviceHandlerProject: $this")
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
