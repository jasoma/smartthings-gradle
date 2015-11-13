package com.github.jasoma.stsync.ide

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.Jsoup

import javax.script.ScriptException

/**
 * Encapsulates the HTTP api exposed by the SmartThings IDE. As the IDE uses a combination of JSON responses and server side rendering
 * this class does both JSON parsing and HTML scraping to recover the necessary information.
 */
class WebIDE {

    private static final String HOST = "https://graph.api.smartthings.com"
    private static final String SESSION_COOKIE_NAME = "JSESSIONID"

    def Map<String, String> cookies = [:]
    def Map<String, String> headers = [:]
    def loggedIn

    /**
     * Connect to a specific path on the WebIDE host. If {@link #login(java.lang.String, java.lang.String)} has been called then
     * the stored headers/cookies needed to authenticate will be set.
     *
     * @param path the path to connect to.
     * @return a configured request containing any stored headers or cookies.
     */
    def Connection connect(String path) {
        def connection = (path.startsWith("/")) ? Jsoup.connect("$HOST$path") : Jsoup.connect("$HOST/$path");
        connection.followRedirects(false)
        cookies.each { connection.cookie(it.key, it.value) }
        headers.each { connection.header(it.key, it.value) }
        return connection
    }

    /**
     * Authenticates a user with the WebIDE and saves the session data needing to authenticate subsequent requests.
     *
     * @param username the username to login with.
     * @param password the users password.
     * @throws LoginException if the authentication fails.
     */
    def void login(String username, String password) throws LoginException {
        def response = connect("j_spring_security_check")
            .data("j_username", username)
            .data("j_password", password)
            .method(Connection.Method.POST)
            .execute()

        if (response.statusCode() != 302 || !response.hasCookie(SESSION_COOKIE_NAME)) {
            throw ApiException.unexpectedResult()
        }
        if (response.header('location')?.contains('authfail')) {
            throw new LoginException()
        }
        cookies.putAll(response.cookies())
        loggedIn = true
    }

    /**
     * Fetches the list of apps for this user.
     *
     * @return a list of the apps from the ide server.
     */
    def List<SmartAppProject> apps() {
        ensureLoggedIn()
        def appsPage = connect('/ide/apps').get()
        def tableData = appsPage.select('#smartapp-table tbody tr')

        if (tableData.isEmpty()) {
            throw ApiException.unexpectedResult()
        }
        return tableData.collect { SmartAppProject.fromRow(it, this) }
    }

    /**
     * Fetches all the resource descriptors for a project.
     *
     * @param projectId the id of the project to load resources for.
     * @return the resources for that project/
     */
    def AppResources downloadResourcesList(SmartAppProject project) {
        ensureLoggedIn()
        def connection = connect('/ide/app/getResourceList')
            .data('id', project.id)
            .ignoreContentType(true)
        def response = connection.execute()

        if (response.statusCode() != 200 || !response.contentType().contains('json')) {
            throw ApiException.unexpectedResult()
        }
        return AppResources.fromJson(response.body())
    }

    /**
     * Fetches the user script for an app project.
     *
     * @param project the project to get the script for.
     * @return the entire text of the script.
     */
    def String downloadScript(SmartAppProject project) {
        ensureLoggedIn()
        if (!project.hasScript()) {
            throw new IllegalStateException("No script file was found in the resources for project ${project.name} (${project.id}). " +
                    "Full resource list:\n${JsonOutput.prettyPrint(JsonOutput.toJson(project.rawResources))}")
        }

        def connection = connect('/ide/app/getCodeForResource')
            .data('id', project.id)
            .data('resourceId', project.getScriptEntry()['id'] as String)
            .data('resourceType', 'script')
            .method(Connection.Method.POST)
            .ignoreContentType(true)
        def response = connection.execute()

        if (response.statusCode() != 200 || !response.contentType().contains('groovy')) {
            throw ApiException.unexpectedResult()
        }
        return response.body();
    }

    /**
     * Upload a new version of an app script file to a project.
     *
     * @param project the project to upload the script to.
     * @param script the script contents.
     * @throws ScriptException if the script cannot be compiled on the remote server.
     */
    def void uploadScript(SmartAppProject project, String script) throws ScriptException {
        ensureLoggedIn()
        def connection = connect('/ide/app/compile')
                .data('id', project.id)
                .data('resourceId', project.getScriptEntry()['id'] as String)
                .data('resourceType', 'script')
                .data('code', script)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
        def response = connection.execute()

        if (response.statusCode() != 200 || !response.contentType().concat('json')) {
            throw ApiException.unexpectedResult()
        }
        checkCompileErrors(response)
    }

    /**
     * Fetches the list of device handlers for this user.
     *
     * @return a list of device handlers from the server.
     */
    def List<DeviceHandlerProject> deviceHandlers() {
        ensureLoggedIn()
        def devicePage = connect('/ide/devices').get()
        def tableData = devicePage.select('#devicetype-table tbody tr')

        if (tableData.isEmpty()) {
            throw ApiException.unexpectedResult()
        }
        return tableData.collect { DeviceHandlerProject.fromRow(it, this) }
    }

    /**
     * Fetches the user script for a device handler project.
     *
     * @param project the project to get the script for.
     * @return the entire text of the script.
     */
    def String downloadScript(DeviceHandlerProject project) {
        ensureLoggedIn()
        def editor = connect("/ide/device/editor/${project.id}").get()
        def codeBlock = editor.select('#code')

        if (codeBlock.isEmpty()) {
            throw ApiException.unexpectedResult()
        }
        return codeBlock.first().text()
    }

    /**
     * Upload a new version of a device script file to a project.
     * <p>
     * <strong>
     *     WARNING: Unlike when uploading an app script the WebIDE currently <em>will</em> save non-compiling device handler scripts.
     *     If compilation fails the script must be manually rolled back with another upload.
     * </strong>
     * @param project the project to upload the script to.
     * @param script the script contents.
     * @throws ScriptException if the script cannot be compiled on the remote server.
     */
    def void uploadScript(DeviceHandlerProject project, String script) {
        ensureLoggedIn()
        def connection = connect('/ide/device/compile')
                .data('id', project.id)
                .data('code', script)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
        def response = connection.execute()

        if (response.statusCode() != 200 || !response.contentType().concat('json')) {
            throw ApiException.unexpectedResult()
        }
        checkCompileErrors(response)
    }

    private def ensureLoggedIn() {
        if (!loggedIn) {
            throw new IllegalStateException("`login(username, password) must be called before accessing other methods on the WebIDE")
        }
    }

    private def checkCompileErrors(Response response) {
        def parser = new JsonSlurper()
        def results = parser.parseText(response.body())
        def errors = results['errors']
        if (!errors.isEmpty()) {
            throw new ScriptException("The script failed to compile on the remote server, errors:\n\t${errors.join('\n\t')}")
        }
    }

    static class LoginException extends Exception {
        LoginException() {
            super('Could not login to the WebIDE, check your username and password are correct')
        }
    }
}
