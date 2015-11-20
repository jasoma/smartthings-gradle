package com.github.jasoma.stsync.gradle
import difflib.DiffUtils
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutput.Style
import org.gradle.logging.StyledTextOutputFactory

/**
 * Diffs the local version of the project script with the version held in the WebIDE.
 */
class DiffTask extends SmartThingsTask {

    private static final String ERROR_COLOR = 'org.gradle.color.error'
    private static final String SUCCESS_COLOR = 'org.gradle.color.success'
    private static final String HEADER_COLOR = 'org.gradle.color.header'

    boolean color = true

    DiffTask() {
        description = 'Diff the local project script with the version held in the WebIDE'
    }

    @TaskAction
    def diff() {
        def local = project.file(scriptFileName()).readLines()
        def remote = ext.project.downloadScript().readLines()
        def patch = DiffUtils.diff(remote, local)
        def diff = DiffUtils.generateUnifiedDiff('WebIDE Version', 'Local Version', remote, patch, 3)


        if (color) {
            colorDiff(diff)
        }
        else {
            if (diff.isEmpty()) {
                println("Contents are identical")
            }
            else {
                diff.each { println(it) }
            }
        }
    }

    def colorDiff(List<String> lines) {
        def error = System.getProperty(ERROR_COLOR)
        def success = System.getProperty(ERROR_COLOR)
        def header = System.getProperty(ERROR_COLOR)

        try {
            System.setProperty(ERROR_COLOR, 'RED')
            System.setProperty(SUCCESS_COLOR, 'GREEN')
            System.setProperty(HEADER_COLOR, 'YELLOW')

            def out = services.get(StyledTextOutputFactory).create("diff")

            if (lines.isEmpty()) {
                out.withStyle(Style.Header).println("Contents are identical")
                return
            }

            lines.each { line ->
                if (line.startsWith('-')) {
                    out.withStyle(Style.Error).println(line)
                } else if (line.startsWith('+')) {
                    out.withStyle(Style.Success).println(line)
                } else if (line.startsWith('@@')) {
                    out.withStyle(Style.Header).println(line)
                } else {
                    out.println(line)
                }
            }
        } finally {
            resetProperty(ERROR_COLOR, error)
            resetProperty(SUCCESS_COLOR, success)
            resetProperty(HEADER_COLOR, header)
        }
    }

    def private resetProperty(String key, String oldValue) {
        if (oldValue == null) {
            System.clearProperty(key)
        }
        else {
            System.setProperty(key, oldValue)
        }
    }

}
