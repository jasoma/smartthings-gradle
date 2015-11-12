package com.github.jasoma.stsync.api

import groovy.transform.TupleConstructor

/**
 * Combines the immutable {@link SmartAppProject} and {@link ProjectResources} into a single object.
 */
@TupleConstructor
class ProjectAndResources {

    @Delegate final SmartAppProject project
    @Delegate final ProjectResources resources

}
