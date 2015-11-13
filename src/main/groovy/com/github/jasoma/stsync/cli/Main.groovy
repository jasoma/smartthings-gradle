package com.github.jasoma.stsync.cli

import com.github.jasoma.stsync.ide.WebIDE

/**
 * Entry point for the CLI version of the sync utility.
 */
class Main {

    def static void main(String[] args) {
        def options = Options.parse(args)

        if (options.help) {
            options.usage()
        }
        else if (options.config && [options.raw.namespace, options.raw.username, options.raw.password].any()) {
            saveConfig(options)
            printConfig(options)
        }
        else if (options.config) {
            printConfig(options)
        }
        else if (options.list == 'apps') {
            listApps(options)
        }
        else if (options.list == 'devices') {
            listDevices(options)
        }
    }

    /**
     * Prints the stored options from the {@code gradle.properties} file.
     */
    def static printConfig(options) {
        println("Stored Configuration [${options.defaultsFilePath()}]")
        println("Username:  ${options.defaults[Options.USERNAME_KEY]}")
        println("Password:  ${options.defaults[Options.PASSWORD_KEY]}")
        println("Namespace: ${options.defaults[Options.NAMESPACE_KEY]}")
    }

    /**
     * Saves any explicitly passed config values to the {@code gradle.properties} file.
     */
    def static saveConfig(Options options) {
        def properties = options.defaults
        [
                (Options.NAMESPACE_KEY): options.raw.namespace,
                (Options.PASSWORD_KEY): options.raw.password,
                (Options.USERNAME_KEY): options.raw.username
        ].each { entry ->
            if (entry.value != null && entry.value != false) {
                properties.setProperty(entry.key, entry.value as String)
            }
        }
        def file = Options.defaultsFilePath().toFile()
        def stream = new FileOutputStream(file)
        properties.store(stream, null)
        stream.close()
    }

    /**
     * Lists all the apps for the user in the Web IDE.
     */
    def static listApps(Options options) {
        def ide = new WebIDE()
        ide.login(options.requireUsername(), options.requirePassword())
        def apps = ide.apps();
        println("Found SmartApps (namespace : name) =>")
        apps.each { println("${it.namespace} : ${it.name}")}
    }

    /**
     * Lists all the device handlers for the user in the Web IDE.
     */
    def static listDevices(Options options) {
        def ide = new WebIDE()
        ide.login(options.requireUsername(), options.requirePassword())
        def devices = ide.deviceHandlers();
        println("Found Device Handlers (namespace : name) =>")
        devices.each { println("${it.namespace} : ${it.name}")}
    }
}
