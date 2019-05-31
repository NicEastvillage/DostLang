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
        |int a = 5
        |if (b > 0) {
        |   a = a * 2 * (a - 1 + 1)
        |}
        |for (i : 0 ..= c) {
        |   while (i == -1) { }
        |}
        |bool a = a != (0)
    """.trimMargin()

    tryCompile(CharStreams.fromString(source))
}

fun tryCompile(source: CharStream) {
    try {
        compile(source)

    } catch (e: TerminatedCompilationException) {

        System.err.println("Compilation failed!")
        System.err.println("Printing errors (${ErrorLog.allErrors().size}) ...")
        ErrorLog.printAllErrors()
        System.err.println("Printing warnings (${ErrorLog.allWarnings().size}) ...")
        ErrorLog.allErrors()

    } catch (e: RuntimeException) {

        System.err.println("Compiler crashed!")
        System.err.println("Printing errors (${ErrorLog.allErrors().size}) ...")
        ErrorLog.printAllErrors()
        System.err.println("Printing warnings (${ErrorLog.allWarnings().size}) ...")
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
