package com.github.jasoma.stsync.api
import spock.lang.Shared
import spock.lang.Specification

class WebIDESpec extends Specification {

    @Shared def ide = new WebIDE()

    def "cannot interact with server before login"() {
        when:
        action.call()

        then:
        thrown(IllegalStateException)

        where:
        action << [ide.&apps, ide.&loadResources, ide.&loadScript]
    }

}
