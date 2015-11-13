package com.github.jasoma.stsync.ide

import spock.lang.Shared
import spock.lang.Specification

class AppResourcesSpec extends Specification {

    @Shared def sample =
'''
[{"text":"css","type":"folder","li_attr":{"resource-type":"folder"},"children":[]},{"text":"images","type"
:"folder","li_attr":{"resource-type":"folder"},"children":[]},{"text":"javascript","type":"folder","li_attr"
:{"resource-type":"folder"},"children":[]},{"text":"src","type":"folder","li_attr":{"resource-type":"folder"
},"children":[]},{"text":"views","type":"folder","li_attr":{"resource-type":"folder"},"children":[]}
,{"id":"12345678-0b76-4799-b956-f52166f26717","text":"synctestapp.groovy","type":"file","li_attr":{"resource-type"
:"script","resource-content-type":"text/plain"}}]
'''

    def "it should detect if one of the resources is a groovy script"() {
        def resources = AppResources.fromJson(sample)

        expect:
        resources.hasScript() == true
        resources.getScriptEntry()['id'] == '12345678-0b76-4799-b956-f52166f26717'
    }

    def "it should report no script if there are no `.groovy` files"() {
        def noCode = sample.replace('.groovy', '')
        def resources = AppResources.fromJson(noCode)

        expect:
        resources.hasScript() == false
        resources.getScriptEntry() == null
    }

    def "it should handle entries not having a `text` field"() {
        def noText = sample.replaceAll('"text":"(.*?)",', '')
        def resources = AppResources.fromJson(noText)

        expect:
        resources.hasScript() == false
        resources.getScriptEntry() == null
    }
}
