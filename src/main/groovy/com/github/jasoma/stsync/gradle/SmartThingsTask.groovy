package com.github.jasoma.stsync.gradle

import org.gradle.api.DefaultTask

/**
 * Base class for plugin tasks.
 */
abstract class SmartThingsTask extends DefaultTask {

    @Lazy SmartThingsExtension ext = { project.extensions.getByType(SmartThingsExtension) }()

    SmartThingsTask() {
        group = 'SmartThings'
    }

    /**
     * @return the project relative path of the local groovy file for the project script.
     */
    def scriptFileName() {
        return "${ext.name}.groovy"
    }

}
