package com.github.jasoma.stsync.api

import spock.lang.Shared
import spock.lang.Specification

class SmartAppProjectSpec extends Specification {

    @Shared def ide = Mock(WebIDE)

    def "should not validate if any property is missing"() {
        when:
        project.validate()

        then:
        thrown(ApiException)

        where:
        project << [
                new SmartAppProject(ide: ide, id: null, name: "2", namespace: "3", status: "4", category: "5"),
                new SmartAppProject(ide: ide, id: "1", name: null, namespace: "3", status: "4", category: "5"),
                new SmartAppProject(ide: ide, id: "1", name: "2", namespace: null, status: "4", category: "5"),
                new SmartAppProject(ide: ide, id: "1", name: "2", namespace: "3", status: null, category: "5"),
                new SmartAppProject(ide: ide, id: "1", name: "2", namespace: "3", status: "4", category: null)
        ]
    }

    def "should not validate if `id` is empty or all whitespace"() {
        when:
        project.validate()

        then:
        thrown(ApiException)

        where:
        project << [
                new SmartAppProject(ide: ide, id: '', name: "2", namespace: "3", status: "4", category: "5"),
                new SmartAppProject(ide: ide, id: '    ', name: "2", namespace: "3", status: "4", category: "5"),
                new SmartAppProject(ide: ide, id: '  \t', name: "2", namespace: "3", status: "4", category: "5"),
                new SmartAppProject(ide: ide, id: '\n  ', name: "2", namespace: "3", status: "4", category: "5")
        ]
    }

}
