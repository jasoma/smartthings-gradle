package com.github.jasoma.stsync.gradle
import com.github.jasoma.stsync.ide.WebIDE
import groovy.transform.PackageScope
import groovy.transform.PackageScopeTarget
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Extension object holding configurable properties for the plugin.
 */
@ToString(includeNames = true, includePackage = false)
@PackageScope(PackageScopeTarget.METHODS)
@Slf4j
class SmartThingsExtension {

    enum ProjectType { App, Device }

    /**
     * The SmartThings WebIDE username, will be taken from project properties if not set in the build script.
     */
    String username

    /**
     * The SmartThings WebIDE password, will be taken from project properties if not set in the build script.
     */
    String password

    @PackageScope ProjectType type

    /**
     * The namespace of the project being built.
     */
    String namespace

    /**
     * The name of the project build built.
     */
    String name

    /**
     * A logged in {@link WebIDE} instance.
     */
    @Lazy WebIDE ide = {
        def ide = new WebIDE()
        ide.login(username, password)
        return ide
    }()

    /**
     * The project instance loaded from the ide.
     */
    @Lazy def project = {
        return (type == ProjectType.App) ? findApp() : findDevice()
    }()

    /**
     * Configure the project to build a SmartApp.
     *
     * @param settings settings closure for specifying name and namespace
     */
    public def app(Closure settings) {
        type = ProjectType.App
        doSettings(settings)
    }

    /**
     * Configure the project to build a Device Type Handler.
     *
     * @param settings settings closure for specifying name and namespace
     */
    public def device(Closure settings) {
        type = ProjectType.Device
        doSettings(settings)
    }

    def doSettings(Closure settings) {
        settings.delegate = this
        settings.resolveStrategy = Closure.DELEGATE_ONLY
        settings.call()
    }

    def readDefaults(Project project) {
        username = project.property('smartthings.username')
        password = project.property('smartthings.password')
    }

    def findApp() {
        def apps = ide.apps()
        def target = apps.find { it.name == name && it.namespace == namespace }

        if (target == null) {
            notFound(apps)
        }
        return target
    }

    def findDevice() {
        def devices = ide.deviceHandlers()
        def target = devices.find { it.name == name && it.namespace == namespace }

        if (target == null) {
            notFound(devices)
        }
        return target
    }

    def notFound(List list) {
        def found = list.join("\n\t")
        throw new GradleException("Could not find the target ${projectString()}, found:\n\t$found")
    }

    def projectString() {
        return "${(type == ProjectType.App) ? 'SmartApp' : 'DeviceHandler'}(${namespace} : ${name})"
    }

}
