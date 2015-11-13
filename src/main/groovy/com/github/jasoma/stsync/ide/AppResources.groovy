package com.github.jasoma.stsync.ide

import groovy.json.JsonSlurper
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

/**
 * Very basic wrapper for the list of project resources returned from the ide server. Can scan the list
 * to detect the single user submitted groovy script.
 */
@Immutable
@ToString(includeNames = true, includePackage = false, cache = true)
class AppResources {

    List<Map> rawResources

    /**
     * Parses the json list from the server into a new instance.
     *
     * @param json the raw json response string.
     * @return the constructed resources instance.
     */
    def static AppResources fromJson(String json) {
        def parser = new JsonSlurper()
        return new AppResources(rawResources: (List) parser.parseText(json))
    }

    /**
     * Checks if the resource list contains a script file or not.
     *
     * @return true if a script file could be detected, false otherwise.
     */
    def boolean hasScript() {
        return getScriptEntry() != null
    }

    /**
     * @return the resource description map for the project groovy script if it could be found.
     */
    def Map getScriptEntry() {
        return rawResources.find { it['text']?.contains('.groovy') }
    }

}
