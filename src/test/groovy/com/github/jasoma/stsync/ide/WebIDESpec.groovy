package com.github.jasoma.stsync.ide
import spock.lang.Shared
import spock.lang.Specification

class WebIDESpec extends Specification {

    @Shared def ide = new WebIDE()
    @Shared def app = Mock(SmartAppProject)
    @Shared def device = Mock(DeviceHandlerProject)

    def "cannot interact with server before login"() {
        when:
        action.call()

        then:
        thrown(IllegalStateException)

        where:
        action << [ide.&apps,
                   ide.&downloadResourcesList.curry(app),
                   ide.&downloadScript.curry(app),
                   ide.&uploadScript.curry(app, ''),
                   ide.&deviceHandlers,
                   ide.&downloadScript.curry(device),
                   ide.&uploadScript.curry(device, '')]
    }

}
