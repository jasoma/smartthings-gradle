package com.github.jasoma.stsync.integration

import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class CLIIntegrationSpec extends Specification {

    @Shared File downloadRoot = new File("build/tmp")
    @Shared String jarPath = System.getenv("stsync.jar")

    def "should show the test app and the test device in list"() {
        def output

        when:
        output = exec("--list")

        then:
        output.readLines().find { it.contains('SyncTestApp') } != null
        output.readLines().find { it.contains('SyncTestDevice') } != null
    }

    def "should be able to download the test app"() {
        def projectDir = new File(downloadRoot, 'SyncTestApp')

        when:
        exec("--app", "SyncTestApp", '--root', downloadRoot.getAbsolutePath())

        then:
        assert projectDir.exists()
        assert Files.exists(projectDir.toPath().resolve('SyncTestApp.groovy'))
        assert Files.exists(projectDir.toPath().resolve('build.gradle'))
    }

    def "should be able to download the test device"() {
        def projectDir = new File(downloadRoot, 'SyncTestDevice')

        when:
        exec("--device", "SyncTestDevice", '--root', downloadRoot.getAbsolutePath())

        then:
        assert projectDir.exists()
        assert Files.exists(projectDir.toPath().resolve('SyncTestDevice.groovy'))
        assert Files.exists(projectDir.toPath().resolve('build.gradle'))
    }

    def exec(String... args) {
        def cmd = "java -jar ${jarPath} ${args.join(' ')}"
        def process = cmd.execute()
        def output = new StringWriter()
        process.consumeProcessOutputStream(output)
        process.waitFor()
        if (process.exitValue() != 0) {
            throw new RuntimeException("Command ${cmd} failed with exit code: ${process.exitValue()}. output:\n$output")
        }
        return output.toString()
    }
}
