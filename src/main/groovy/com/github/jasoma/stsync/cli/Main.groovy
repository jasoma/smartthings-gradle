package com.github.jasoma.stsync.cli
import com.github.jasoma.stsync.ide.WebIDE
import com.github.jasoma.stsync.ide.WebIDE.LoginException
import groovy.text.GStringTemplateEngine

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

/**
 * Entry point for the CLI version of the sync utility.
 */
class Main {

    def static void main(String[] args) {
        def options = Options.parse(args)

        if (options.help || args.length == 0) {
            options.usage()
        }
        else if (options.config && [options.raw.namespace, options.raw.username, options.raw.password].any()) {
            saveConfig(options)
            printConfig(options)
        }
        else if (options.config) {
            printConfig(options)
        }
        else if (options.list) {
            listProjects(options)
        }
        else if (options.app) {
            createAppProject(options)
        }
        else if (options.device) {
            createDeviceProject(options)
        }
        else {
            println("Unknown arguments $args")
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
     * List all app and device handler projects store in the Web IDE
     */
    def static listProjects(Options options) {
        def ide = login(options)
        println("User: ${options.username}\n")

        def apps = ide.apps();
        println("SmartApps (namespace : name) =>")
        apps.each { println("${it.namespace} : ${it.name}")}
        println()

        def devices = ide.deviceHandlers();
        println("Device Handlers (namespace : name) =>")
        devices.each { println("${it.namespace} : ${it.name}")}
    }

    /**
     * Download an app script from the Web IDE and create a local gradle build for it.
     */
    def static createAppProject(Options options) {
        def ide = login(options)
        println("Downloading app [${options.requireNamespace()} : ${options.app}]...")

        def project = ide.apps().find { it.name == options.app && it.namespace == options.requireNamespace() }
        println("Setting up project in ${Paths.get(options.root, options.app)}...")
        def localProject = LocalProject.setup(options, options.app)

        localProject.projectScript.write(project.downloadScript(), StandardCharsets.UTF_8.name())
        println("Wrote app script to ${localProject.projectScript.absolutePath}...")

        def template = loadTemplate(options, 'app', options.app)
        localProject.buildScript.write(template.toString(), StandardCharsets.UTF_8.name())
        println("Created gradle build script...")
        println("Done!")
    }

    /**
     * Download a device handler script from the Web IDE and create a local gradle build for it.
     */
    def static createDeviceProject(Options options) {
        def ide = login(options)
        println("Setting up project in ${Paths.get(options.root, options.device)}...")

        def project = ide.deviceHandlers().find { it.name == options.device && it.namespace == options.requireNamespace() }
        println("Setting up project in ${Paths.get(System.getProperty('user.dir'), options.device)}...")
        def localProject = LocalProject.setup(options, options.device)

        localProject.projectScript.write(project.downloadScript(), StandardCharsets.UTF_8.name())
        println("Wrote device handler script to ${localProject.projectScript.absolutePath}...")

        def template = loadTemplate(options, 'device', options.device)
        localProject.buildScript.write(template.toString().trim(), StandardCharsets.UTF_8.name())
        println("Created gradle build script...")
        println("Done!")
    }

    private def static WebIDE login(Options options) {
        def ide = new WebIDE()
        try {
            ide.login(options.requireUsername(), options.requirePassword())
        } catch (LoginException e) {
            println("Could not login to the WebIDE, check your credentials with `at-sync --conifg`")
            System.exit(-1)
        }
        return ide
    }

    private def static createProjectDirectory(String name) {
        def projectDir = new File(name)
        println("Setting up project in '${projectDir.absolutePath}'...")
        if (projectDir.exists()) {
            println("Failed: Path ${projectDir.absolutePath} already exists")
            System.exit(-1)
        }
        if (!projectDir.mkdir()) {
            println("Failed: Could not create project directory ${projectDir.absolutePath}")
            System.exit(-1)
        }
        return projectDir
    }

    private def static loadTemplate(Options options, String type, String name) {
        def engine = new GStringTemplateEngine()
        def resource = getClass().getResourceAsStream("/com/github/jasoma/stsync/gradle/build.gradle.template")
        def template = engine.createTemplate(new InputStreamReader(resource, StandardCharsets.UTF_8))
        def args = [
                namespace: options.requireNamespace(),
                username: options.raw.username,
                password: options.raw.password,
                name: name,
                type: type
        ]
        return template.make(args)
    }
}
