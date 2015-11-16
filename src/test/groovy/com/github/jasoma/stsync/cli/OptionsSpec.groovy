package com.github.jasoma.stsync.cli
import org.apache.commons.cli.MissingArgumentException
import spock.lang.Shared
import spock.lang.Specification

class OptionsSpec extends Specification {

    @Shared Properties cleanDefaults
    @Shared Properties testDefaults
    @Shared File defaultsFile = Options.defaultsFilePath().toFile()

    def setupSpec() {
        cleanDefaults = new Properties()
        cleanDefaults.load(new FileReader(defaultsFile))
        testDefaults = new Properties()
    }

    def cleanupSpec() {
        cleanDefaults.store(new FileWriter(defaultsFile), null)
    }

    def setDefault(String key, String value) {
        if (value != null) {
            testDefaults.setProperty(key, value)
        } else {
            testDefaults.remove(key)
        }
        testDefaults.store(new FileWriter(defaultsFile), null)
    }

    def "it should use values from the command line args before the defaults"() {
        def options = Options.parse(args)

        expect:
        options."$key" == value

        where:
        key << ['username', 'password', 'namespace']
        value << ["cli.username", "cli.password", "cli.namespace"]
        args << [['-u', "cli.username"], ['-p', "cli.password"], ['-n', "cli.namespace"]]
    }

    def "it should use values from the defaults if not specified on the command line"() {
        setDefault(Options.NAMESPACE_KEY, "default.namespace")
        setDefault(Options.PASSWORD_KEY, "default.password")
        setDefault(Options.USERNAME_KEY, "default.username")
        def options = Options.parse(args)

        expect:
        options."$key" == value

        where:
        key << ['username', 'password', 'namespace']
        value << ['default.username', 'default.password', 'default.namespace']
        args << [[], [], []]
    }

    def "an exception should be thrown if no namespace, username, or password is available"() {
        defaultsFile.delete()

        when:
        accessor.call()

        then:
        thrown(MissingArgumentException)

        where:
        accessor << [
                { -> Options.parse([]).requireNamespace() },
                { -> Options.parse([]).requirePassword() },
                { -> Options.parse([]).requireUsername() }
        ]
    }

}
