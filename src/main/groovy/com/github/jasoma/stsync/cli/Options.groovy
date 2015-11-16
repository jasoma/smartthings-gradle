package com.github.jasoma.stsync.cli

import org.apache.commons.cli.MissingArgumentException

import java.nio.file.Paths

/**
 * Wraps a parsed {@link CliBuilder#options} to check the user-local gradle properties for default values for
 * username, password, and namespace. Otherwise behaves the same as the wrapped options.
 */
class Options {

    /** Property key for the username in the defaults file */
    static final String USERNAME_KEY = 'smartthings.username'

    /** Property key for the password in the defaults file */
    static final String PASSWORD_KEY = 'smartthings.password'

    /** Property key for the namespace in the defaults file */
    static final String NAMESPACE_KEY = 'smartthings.namespace'

    @Delegate OptionAccessor raw
    Properties defaults

    private Options(raw) {
        this.raw = raw
        this.defaults = loadDefaults()
    }

    /**
     * Parse the program arguments into an option set.
     *
     * @param args the program args.
     * @return the parsed options.
     */
    def static parse(args) {
        return new Options(optionParser().parse(args))
    }

    private def static CliBuilder optionParser() {
        def cli = new CliBuilder(usage: 'st-sync [command] [options]')
        cli.config('read or set the default values for username, password, and namespace')
        cli.u(longOpt: 'username', args: 1, argName: 'username', 'specify your SmartThings username')
        cli.p(longOpt: 'password', args: 1, argName: 'password', 'specify your SmartThings password')
        cli.n(longOpt: 'namespace', args: 1, argName: 'namespace', 'specify the namespace for the app or device project')
        cli.l(longOpt: 'list', 'list all available apps and devices for the user')
        cli.app(args: 1, argName: 'name', 'create a local project for a SmartApp')
        cli.device(args: 1, argName: 'name', 'create a local project for a DeviceHandler')
        cli.help('show this message')
        cli.footer = """
examples:
  # set the default username, password, and namespace
  st-sync --config -u me@me.com -p secret -n com.me

  # list all your apps
  st-sync -l apps

  # list all your devices
  st-sync -l devices

  # download and create a gradle project for 'MyApp'
  st-sync --app MyApp

  # download and create a gradle project for 'MyDevice'
  st-sync --device MyDevice
"""
        return cli
    }

    /**
     * Loads the user-local {@code gradle.properties} file if it exists to load and save defaults.
     *
     * @return the contents of {@code gradle.properties} if it exists or an empty instance if not.
     */
    def static Properties loadDefaults() {
        def file = defaultsFilePath().toFile()
        def properties = new Properties()
        if (file.exists()) {
            properties.load(new FileReader(file))
        }
        return properties
    }

    /**
     * @return the path where the defaults property file will be looked for.
     */
    def static defaultsFilePath() {
        def gradleHome = System.getenv("GRADLE_USER_HOME") ?: Paths.get(System.getProperty("user.home"), '.gradle').toString()
        return Paths.get(gradleHome, 'gradle.properties')
    }

    /**
     * Delegate missing properties to the wrapped {@link CliBuilder#options}.
     */
    def propertyMissing(String name) {
        return raw."$name"
    }

    /**
     * Delegate missing properties to the wrapped {@link CliBuilder#options}.
     */
    def propertyMissing(String name, def arg) {
        raw."$name" = arg
    }

    /**
     * @return the namespace option from the program args or the defaults in that order.
     */
    def getNamespace() {
        return raw.namespace ?: defaults[NAMESPACE_KEY]
    }

    /**
     * @return {@link #getNamespace()}.
     * @throws MissingArgumentException if a namespace is not present.
     */
    def requireNamespace() throws MissingArgumentException {
        def namespace = getNamespace()
        if (namespace == null) {
            throw new MissingArgumentException("No namespace specified either in the command or the defaults, cannot continue")
        }
        return namespace
    }

    /**
     * @return the username option from the program args or the defaults in that order.
     */
    def getUsername() {
        return raw.username ?: defaults[USERNAME_KEY]
    }

    /**
     * @return {@link #getUsername()}.
     * @throws MissingArgumentException if a username is not present.
     */
    def requireUsername() throws MissingArgumentException {
        def username = getUsername()
        if (username == null) {
            throw new MissingArgumentException("No username specified either in the command or the defaults, cannot continue")
        }
        return username
    }

    /**
     * @return the password option from the program args or the defaults in that order.
     */
    def getPassword() {
        return raw.password ?: defaults[PASSWORD_KEY]
    }

    /**
     * @return {@link #getPassword()}.
     * @throws MissingArgumentException if a password is not present.
     */
    def requirePassword() throws MissingArgumentException {
        def password = getPassword()
        if (password == null) {
            throw new MissingArgumentException("No password specified either in the command or the defaults, cannot continue")
        }
        return password
    }

    def usage() {
        optionParser().usage()
    }

}
