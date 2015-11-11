package com.github.jasoma.stsync.integration

import com.github.jasoma.stsync.api.WebIDE
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class WebIDEIntegrationSpec extends Specification {

    @Shared String username = System.getenv("username")
    @Shared String password = System.getenv("password")
    @Shared WebIDE ide = new WebIDE()

    def "login should succeed"() {
        when:
        ide.login(username, password)

        then:
        notThrown(Exception)
        ide.loggedIn == true
        ide.cookies.isEmpty() == false
    }

    def "subsequent requests should be made as the user"() {
        when:
        def home = ide.connect("/").get()

        then:
        def name = home.select(".username")
        name.isEmpty() == false
        name.first().text() == username
    }

}
