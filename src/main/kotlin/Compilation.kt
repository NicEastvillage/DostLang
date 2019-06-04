package dk.eastvillage.dost

import dk.eastvillage.dost.antlr.DostLexer
import dk.eastvillage.dost.antlr.DostParser
import dk.eastvillage.dost.ast.BuildAstVisitor
import dk.eastvillage.dost.contextual.ContextualAnalysisVisitor
import dk.eastvillage.dost.interpreter.Interpreter
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.PrintStream


class CompilationSettings(
    val source: CharStream,
    val doPrettyPrinting: Boolean = false,
    val stdout: PrintStream = System.out,
    val stderr: PrintStream = System.err
)

enum class TerminationTime {
    CORRECTLY, DURING_PARSING, DURING_ANALYSIS, DURING_INTERPRETATION
}

class CompilationInfo(
    val settings: CompilationSettings,
    val errors: ErrorLog = ErrorLog(settings),
    var termination: TerminationTime = TerminationTime.CORRECTLY
)

fun tryCompile(settings: CompilationSettings): CompilationInfo {

    val info = CompilationInfo(settings)

    try {
        compile(settings, info)

    } catch (e: TerminatedCompilationException) {

        settings.stderr.println("Printing errors (${info.errors.allErrors().size}) and warnings (${info.errors.allWarnings().size}) ...")
        info.errors.printAllErrors()

    } catch (e: RuntimeException) {

        settings.stderr.println("Compiler crashed!")
        settings.stderr.println("Printing errors (${info.errors.allErrors().size}) and warnings (${info.errors.allWarnings().size}) ...")
        info.errors.printAllErrors()
        e.printStackTrace()
    }

    return info
}

private fun compile(settings: CompilationSettings, info: CompilationInfo) {

    // Syntactical analysis
    info.termination = TerminationTime.DURING_PARSING
    val lexer = DostLexer(settings.source)
    val tokenStream = CommonTokenStream(lexer)
    val parser = DostParser(tokenStream)
    val ast = BuildAstVisitor().visit(parser.start())
    info.errors.assertNoErrors()

    if (settings.doPrettyPrinting) {
        PrettyPrinter(settings).start(ast)
        info.termination = TerminationTime.CORRECTLY
    }

    // Contextual analysis
    info.termination = TerminationTime.DURING_ANALYSIS
    ContextualAnalysisVisitor(info).analyse(ast)
    info.errors.assertNoErrors()

    // Interpret
    info.termination = TerminationTime.DURING_INTERPRETATION
    try {
        Interpreter(info).start(ast)
    } catch (e: RuntimeException) {
        settings.stderr.println("Exception: ${e.message}")
        e.printStackTrace()
        return
    }

    info.termination = TerminationTime.CORRECTLY
}