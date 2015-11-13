package com.github.jasoma.stsync.integration
import com.github.jasoma.stsync.api.DeviceHandlerProject
import com.github.jasoma.stsync.api.SmartAppProject
import com.github.jasoma.stsync.api.WebIDE
import spock.lang.Ignore
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
    @Shared DeviceHandlerProject testDevice

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
        testApp.resources

        then:
        testApp.resources != null
    }

    def "the ide should be able to recover the script for the test app"() {
        when:
        def script = testApp.downloadScript()

        then:
        script.readLines().first() == ('// st-gradle test!')
    }

    def "the ide should be able to upload a new version of an app script"() {
        when:
        def script = testApp.downloadScript().readLines()
        def dateLine = "// date: $now"
        script.set(1, dateLine)
        testApp.uploadScript(script.join('\n'))

        then:
        noExceptionThrown()
    }

    def "the changes to the app script should be visible in the next download"() {
        when:
        def script = testApp.downloadScript().readLines()

        then:
        "${script[1]}" == "// date: $now"
    }

    def "compilation errors in the app script should result in an exception"() {
        when:
        def script = testApp.downloadScript().readLines()
        script.set(1, "this won't compile you know")
        testApp.uploadScript(script.join('\n'))

        then:
        thrown(ScriptException)
    }

    def "non compiling app scripts should not be saved"() {
        when:
        def script = testApp.downloadScript().readLines()

        then:
        assert script[1].startsWith("// date")
    }

    def "'SyncTestDevice' should be in the devices list"() {
        when:
        def devices = ide.deviceHandlers()
        testDevice = devices.find { it.name == 'SyncTestDevice' }

        then:
        testDevice != null
    }

    def "the ide should be able to recover the script for the test device"() {
        when:
        def script = testDevice.downloadScript().readLines()

        then:
        assert script[0] == '// st-gradle test!'
    }

    def "the ide should be able to upload a new version of a device script"() {
        when:
        def script = testDevice.downloadScript().readLines()
        def dateLine = "// date: $now"
        script.set(1, dateLine)
        testDevice.uploadScript(script.join('\n'))

        then:
        noExceptionThrown()
    }

    def "the changes to the device script should be visible in the next download"() {
        when:
        def script = testDevice.downloadScript().readLines()

        then:
        "${script[1]}" == "// date: $now"
    }

    def "compilation errors in the app device should result in an exception"() {
        when:
        def script = testDevice.downloadScript().readLines()
        script.set(1, "this won't compile you know")
        testDevice.uploadScript(script.join('\n'))

        then:
        thrown(ScriptException)
    }

    @Ignore('currently the ide _will_ save non-compiling device scripts unlike app scripts. keeping this method here in case that is fixed')
    def "non compiling device scripts should not be saved"() {
        when:
        def script = testDevice.downloadScript().readLines()

        then:
        assert script[1].startsWith("// date")
    }
}
