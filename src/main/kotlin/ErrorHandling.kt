package dk.eastvillage.dost

import java.io.PrintStream
import java.util.stream.Stream

/**
 * A Loggable can be logged in the ErrorLog.
 * @param sctx the source context from which this was created
 * @param description the description of the logged thing
 * @see ErrorLog
 */
abstract class Loggable(
    val sctx: SourceContext?,
    val description: String
) : java.lang.RuntimeException(description)

/**
 * A compiler error. All errors originates from some context from the source program. Errors stops the compilation
 * after the current phase is complete.
 * @see ErrorLog
 */
open class CompileError(sctx: SourceContext?, description: String) : Loggable(sctx, description)

/**
 * A compiler warning. All warnings originates from some context from the source program. Warnings does not
 * stop the compilation.
 * @see ErrorLog
 */
open class CompileWarning(sctx: SourceContext?, description: String) : Loggable(sctx, description)

/**
 * The ErrorLog holds all compile errors so far.
 */
class ErrorLog(
    val settings: CompilationSettings
) {

    private val errors: MutableList<CompileError> = mutableListOf()
    private val warnings: MutableList<CompileWarning> = mutableListOf()

    operator fun plusAssign(error: CompileError) = log(error)
    operator fun plusAssign(warning: CompileWarning) = log(warning)

    fun log(error: CompileError) {
        errors += error
    }

    fun log(warning: CompileWarning) {
        warnings += warning
    }

    /**
     * Assert that there are no errors. Throws a TerminatedCompilationException if there are any errors.
     */
    fun assertNoErrors() {
        if (hasErrors()) {
            throw TerminatedCompilationException("Errors occurred.")
        }
    }

    fun hasErrors(): Boolean = errors.size > 0

    fun hasWarnings(): Boolean = warnings.size > 0

    /**
     * Prints all errors in a nicely formatted way. If a stream of source code lines is provided, the printing will be
     * verbose where the exact position of the error's source context will be displayed.
     */
    fun printAllErrors(lines: Stream<String>? = null) {
        if (hasErrors()) {
            if (lines == null) {
                printAll("Error", errors, settings.stderr)
            } else {
                printAllVerbosely("Error", errors, lines, settings.stderr)
            }
        }
    }

    /**
     * Prints all warnings in a nicely formatted way. If a stream of source code lines is provided, the printing will be
     * verbose where the exact position of the warnings's source context will be displayed.
     */
    fun printAllWarnings(lines: Stream<String>? = null) {
        if (hasWarnings()) {
            if (lines == null) {
                printAll("Warning", warnings, settings.stderr)
            } else {
                printAllVerbosely("Warning", warnings, lines, settings.stderr)
            }
        }
    }

    /**
     * Prints all the provided loggables in a nicely formatted way. The printing will be verbose where the exact
     * position of the loggable's source context will be displayed.
     * @see printAll
     */
    private fun printAllVerbosely(type: String, loggables: List<Loggable>, lines: Stream<String>, printStream: PrintStream) {

        val sortedErrors = loggables.sortedWith(compareBy<Loggable> {
            if (it.sctx == null) 0 else it.sctx.lineNumber
        }.thenBy {
            if (it.sctx == null) 0 else it.sctx.charPositionInLine
        })

        // Line index initialised to 1, as lines are not zero-indexed
        var currentLineIndex = 1
        var currentErrorIndex = 0

        // Find the line where the errors occurred so they can be printed
        for (line in lines) {
            var error = sortedErrors[currentErrorIndex]

            while (error.sctx == null || error.sctx!!.lineNumber == currentLineIndex) {

                // Print the error
                if (error.sctx == null) {
                    printStream.println("$type: ${error.description}")
                } else {
                    printStream.println("$type at ${error.sctx}: ${error.description}")
                    // print the source line and a pointer to the context location
                    printStream.println(line)
                    printStream.println(errorPointerString(error.sctx!!))
                }

                // Check next error. It might be on the same line
                currentErrorIndex++

                if (currentErrorIndex == sortedErrors.size) {
                    return // No more errors. We are done
                }

                error = sortedErrors[currentErrorIndex]
            }
            currentLineIndex++
        }
    }

    /**
     * Prints all the provided loggables to the print stream. This will not be verbose.
     * @see printAllVerbosely
     */
    private fun printAll(type: String, loggables: List<Loggable>, printStream: PrintStream) {
        val sortedErrors = loggables.sortedWith(compareBy<Loggable> {
            if (it.sctx == null) 0 else it.sctx.lineNumber
        }.thenBy {
            if (it.sctx == null) 0 else it.sctx.charPositionInLine
        })

        sortedErrors.forEach {
            if (it.sctx == null) {
                printStream.println("$type: ${it.description}")
            } else {
                printStream.println("$type at ${it.sctx.position()}: ${it.description}")
            }
        }
    }

    fun allErrors(): List<CompileError> = errors

    fun allWarnings(): List<CompileWarning> = warnings

    fun reset() {
        errors.clear()
        warnings.clear()
    }

    /**
     * Returns a string consisting of a number of spaces followed by a ^. This string is used to point to the exact
     * position of the error in error messages. E.g. "`      ^`"
     */
    private fun errorPointerString(sctx: SourceContext): String {
        val pointerStringBuilder = StringBuilder()
        repeat(sctx.charPositionInLine) {
            pointerStringBuilder.append(" ")
        }
        pointerStringBuilder.append("^")
        return pointerStringBuilder.toString()
    }
}

/**
 * When a critical error occurs, this error is thrown to terminate the compilation immediately. This differs from
 * the ErrorLog and CompileErrors since multiple of these can occur before the compilation terminates.
 */
class TerminatedCompilationException(msg: String) : RuntimeException(msg)