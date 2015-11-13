package com.github.jasoma.stsync.ide

import spock.lang.Shared
import spock.lang.Specification


class DeviceHandlerProjectSpec extends Specification {

    @Shared def ide = Mock(WebIDE)

    def "should not validate if any property is missing"() {
        when:
        project.validate()

        then:
        thrown(ApiException)

        where:
        project << [
                new DeviceHandlerProject(ide: ide, id: null, name: "2", namespace: "3", status: "4", capabilities: ["5"], oauthEnabled: true),
                new DeviceHandlerProject(ide: ide, id: "1", name: null, namespace: "3", status: "4", capabilities: ["5"], oauthEnabled: true),
                new DeviceHandlerProject(ide: ide, id: "1", name: "2", namespace: null, status: "4", capabilities: ["5"], oauthEnabled: true),
                new DeviceHandlerProject(ide: ide, id: "1", name: "2", namespace: "3", status: null, capabilities: ["5"], oauthEnabled: true),
                new DeviceHandlerProject(ide: ide, id: "1", name: "2", namespace: "3", status: "4", capabilities: null, oauthEnabled: true)
                // no oathEnabled == null since it will just get coerced to false anyway
        ]
    }

    def "should not validate if `id` is empty or all whitespace"() {
        when:
        project.validate()

        then:
        thrown(ApiException)

        where:
        project << [
                new DeviceHandlerProject(ide: ide, id: '', name: "2", namespace: "3", status: "4", capabilities: ["5"], oauthEnabled: true),
                new DeviceHandlerProject(ide: ide, id: '    ', name: "2", namespace: "3", status: "4", capabilities: ["5"], oauthEnabled: true),
                new DeviceHandlerProject(ide: ide, id: '  \t', name: "2", namespace: "3", status: "4", capabilities: ["5"], oauthEnabled: true),
                new DeviceHandlerProject(ide: ide, id: '  \n', name: "2", namespace: "3", status: "4", capabilities: ["5"], oauthEnabled: true)
        ]
    }

}
