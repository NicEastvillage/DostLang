package dk.eastvillage.dost

import dk.eastvillage.dost.antlr.DostLexer
import dk.eastvillage.dost.antlr.DostParser
import dk.eastvillage.dost.ast.BuildAstVisitor
import dk.eastvillage.dost.contextual.ContextualAnalysisVisitor
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.lang.RuntimeException

fun main() {
    println("Hello world")

    val source = """
        |var a = 5
        |if (a > 0) {
        |   a = a * 2 * (a - 1 + 1)
        |   var a = 0
        |}
        |for (i : 0 ..= a) {
        |   while (i == -1.0) { }
        |}
        |var b = (a != (0)) && true
    """.trimMargin()

    tryCompile(CharStreams.fromString(source))
}

fun tryCompile(source: CharStream) {
    try {
        compile(source)

    } catch (e: TerminatedCompilationException) {

        System.err.println("Compilation failed!")
        System.err.println("Printing errors (${ErrorLog.allErrors().size}) and warnings (${ErrorLog.allWarnings().size}) ...")
        ErrorLog.printAllErrors()
        ErrorLog.allErrors()

    } catch (e: RuntimeException) {

        System.err.println("Compiler crashed!")
        System.err.println("Printing errors (${ErrorLog.allErrors().size}) and warnings (${ErrorLog.allWarnings().size}) ...")
        ErrorLog.printAllErrors()
        ErrorLog.allErrors()
        e.printStackTrace()
    }
}

fun compile(source: CharStream) {

    // Syntactical analysis
    val lexer = DostLexer(source)
    val tokenStream = CommonTokenStream(lexer)
    val parser = DostParser(tokenStream)
    val ast = BuildAstVisitor().visit(parser.start())
    ErrorLog.assertNoErrors()

    PrettyPrinter.print(ast, System.out)

    // Contextual analysis
    ContextualAnalysisVisitor.analyse(ast)
    ErrorLog.assertNoErrors()
}
