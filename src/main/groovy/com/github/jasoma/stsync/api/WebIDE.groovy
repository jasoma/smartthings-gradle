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
        def sessionId = response.cookie(SESSION_COOKIE_NAME)

        if (response.statusCode() != 302 || sessionId == null) {
            throw new ApiException("Unexpected response during login, remote API may have changed")
        }
        if (response.header('location')?.contains('authfail')) {
            throw new LoginException()
        }
        cookies[SESSION_COOKIE_NAME] = sessionId
        loggedIn = true
    }

    static class LoginException extends Exception {
        LoginException() {
            super('Could not login to the WebIDE, check your username and password are correct')
        }
    }

    static class ApiException extends RuntimeException {
        ApiException(String message) {
            super(message)
        }
    }

}
