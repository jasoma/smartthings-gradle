package com.github.jasoma.stsync.api

import com.github.jasoma.stsync.api.WebIDE
import spock.lang.Specification

class WebIDESpec extends Specification {

    def ide = new WebIDE()

    def "cannot interact with server before login"() {
        when:
        ide.apps()

        then:
        thrown(IllegalStateException)
    }

}
