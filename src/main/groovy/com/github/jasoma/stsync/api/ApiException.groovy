package com.github.jasoma.stsync.api

/**
 * An exception for when the response from the remote server does not have the format or data expected.
 */
class ApiException extends RuntimeException {

    /**
     * @return an exception with a generic 'unexpected result' message.
     */
    def static ApiException unexpectedResult() {
        return new ApiException("Unexpected response from the SmartThings server, remote API may have changed")
    }

    /**
     * @param message the message for the exception.
     */
    ApiException(String message) {
        super(message)
    }

}
