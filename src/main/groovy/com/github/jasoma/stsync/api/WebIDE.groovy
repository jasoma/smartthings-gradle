package com.github.jasoma.stsync.api

import org.jsoup.Connection
import org.jsoup.Jsoup

/**
 * Created by jason on 11/11/15.
 */
class WebIDE {

    private static final String HOST = "https://graph.api.smartthings.com"
    private static final String SESSION_COOKIE_NAME = "JSESSIONID"

    def cookies = [:]
    def headers = [:]
    def loggedIn

    def Connection connect(String path) {
        def connection = (path.startsWith("/")) ? Jsoup.connect("$HOST$path") : Jsoup.connect("$HOST/$path");
        connection.followRedirects(false)
        connection.ignoreHttpErrors(true)
        return connection
    }

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
