package dk.eastvillage.dost

import dk.eastvillage.dost.antlr.DostLexer
import dk.eastvillage.dost.antlr.DostParser
import dk.eastvillage.dost.ast.BuildAstVisitor
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main() {
    println("Hello world")

    val source = """
        |int a = 5
        |if (a > 0) {
        |   a = a * 2 * (a - 1 + 1)
        |}
        |for (i : 0 ..= a) {
        |   while (false) { }
        |}
        |bool b = a != (0)
    """.trimMargin()

    runProgram(CharStreams.fromString(source))
}

fun runProgram(source: CharStream) {

    // Syntactical analysis
    val lexer = DostLexer(source)
    val tokenStream = CommonTokenStream(lexer)
    val parser = DostParser(tokenStream)
    val ast = BuildAstVisitor().visit(parser.start())

    PrettyPrinter().print(ast, System.out)
}