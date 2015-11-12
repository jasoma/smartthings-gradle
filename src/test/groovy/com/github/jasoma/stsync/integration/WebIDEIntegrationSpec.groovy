package com.github.jasoma.stsync.integration

import com.github.jasoma.stsync.api.ProjectAndResources
import com.github.jasoma.stsync.api.SmartAppProject
import com.github.jasoma.stsync.api.WebIDE
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.script.ScriptException
import java.time.Instant

@Stepwise
class WebIDEIntegrationSpec extends Specification {

    @Shared String username = System.getenv("username")
    @Shared String password = System.getenv("password")
    @Shared Instant now = Instant.now()
    @Shared WebIDE ide = new WebIDE()
    @Shared SmartAppProject testApp
    @Shared ProjectAndResources appWrapper

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
        appWrapper = ide.loadResources(testApp)

        then:
        appWrapper.hasScript() == true
        appWrapper.getScriptEntry() != null
        appWrapper.getScriptEntry()['id'] != null
    }

    def "the ide should be able to recover the script for the test app"() {
        when:
        def script = ide.loadScript(appWrapper)

        then:
        script.readLines().first() == ('// st-gradle test!')
    }

    def "the ide should be able to upload a new version of the script"() {
        when:
        def script = ide.loadScript(appWrapper).readLines()
        def dateLine = "// date: $now"
        script.set(1, dateLine)
        ide.uploadScript(appWrapper, script.join("\n"))

        then:
        noExceptionThrown()
    }

    def "the changes to the script should be visible in the next download"() {
        when:
        def script = ide.loadScript(appWrapper).readLines()

        then:
        "${script[1]}" == "// date: $now"
    }

    def "compilation errors in the script should result in an exception"() {
        when:
        def script = ide.loadScript(appWrapper).readLines()
        script.set(1, "this won't compile you know")
        ide.uploadScript(appWrapper, script.join("\n"))

        then:
        thrown(ScriptException)
    }

    def "non compiling scripts should not be saved"() {
        when:
        def script = ide.loadScript(appWrapper).readLines()

        then:
        assert script[1].startsWith("// date")
    }
}
