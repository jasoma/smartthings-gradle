package com.github.jasoma.stsync.api

import org.jsoup.Connection
import org.jsoup.Jsoup

/**
 * Encapsulates the HTTP api exposed by the SmartThings IDE.
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
        connection.ignoreHttpErrors(true)
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
        return tableData.collect { SmartAppProject.fromRow(it) }
    }

    private def ensureLoggedIn() {
        if (!loggedIn) {
            throw new IllegalStateException("`login(username, password) must be called before accessing other methods on the WebIDE")
        }
    }

    static class LoginException extends Exception {
        LoginException() {
            super('Could not login to the WebIDE, check your username and password are correct')
        }
    }

}
