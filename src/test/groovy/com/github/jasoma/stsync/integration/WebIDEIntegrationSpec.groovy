package com.github.jasoma.stsync.integration

import com.github.jasoma.stsync.api.WebIDE
import spock.lang.Shared
import spock.lang.Specification


class WebIDEIntegrationSpec extends Specification {

    @Shared String username = System.getenv("username")
    @Shared String password = System.getenv("password")

    def ide = new WebIDE()

    def "login should succeed"() {
        when:
        ide.login(username, password)

        then:
        notThrown(Exception)
    }

}
