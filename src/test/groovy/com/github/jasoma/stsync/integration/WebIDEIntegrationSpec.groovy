package com.github.jasoma.stsync.integration

import com.github.jasoma.stsync.api.ProjectResources
import com.github.jasoma.stsync.api.SmartAppProject
import com.github.jasoma.stsync.api.WebIDE
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class WebIDEIntegrationSpec extends Specification {

    @Shared String username = System.getenv("username")
    @Shared String password = System.getenv("password")
    @Shared WebIDE ide = new WebIDE()
    @Shared SmartAppProject testApp
    @Shared ProjectResources resources

    def "login should succeed"() {
        when:
        ide.login(username, password)

        then:
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

    def "'SyncTestApp' should be in the projects list"() {
        when:
        def apps = ide.apps()
        testApp = apps.find { it.name == 'SyncTestApp' }

        then:
        testApp != null
    }

    def "the resources for the test app should load"() {
        when:
        resources = ide.loadResources(testApp.id)

        then:
        resources.hasScript() == true
        resources.getScriptEntry() != null
        resources.getScriptEntry()['id'] != null
    }

    def "the ide should be able to recover the script for the test app"() {
        when:
        def script = ide.loadScript(testApp)

        then:
        script.readLines().first() == ('// st-gradle test!')
    }
}
