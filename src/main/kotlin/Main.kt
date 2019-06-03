package dk.eastvillage.dost

import dk.eastvillage.dost.antlr.DostLexer
import dk.eastvillage.dost.antlr.DostParser
import dk.eastvillage.dost.ast.BuildAstVisitor
import dk.eastvillage.dost.contextual.ContextualAnalysisVisitor
import dk.eastvillage.dost.interpreter.Interpreter
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.RuntimeException


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Needs one argument ('bot-token'), or two ('test [pretty]')")
    } else {
        if (args[0] == "test") {
            val source = """
                |print "Hello world"
                |print "hey\tyou\nthere!"
                |print "concat: " + 5 + true
                |print 12 + 4
            """.trimMargin()
            val pretty = args.size == 2 && args[1] == "pretty"
            tryCompile(CharStreams.fromString(source), System.out, pretty)

        } else {
            val jda = JDABuilder(args[0]).build()
            jda.addEventListener(MessageListener())
        }
    }
}

class MessageListener : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        println("Msg received: " + event.message.contentRaw)
        if (!event.author.isBot) {
            val msg = event.message.contentRaw
            if (msg.startsWith("-dost ") || msg.startsWith("-dost\n")) {
                /* potential message:
                -dost
                ```
                print 2 + 3
                ```
                 */

                val pieces = msg.split("```")
                if (pieces[0].matches(Regex("-dost.*\n"))) {
                    val args = pieces[0].drop(5).split(" ").filter { it != "" }.map { it.trim() }
                    println("Args: $args")
                    val source = pieces[1].trim()

                    // Parse arguments
                    var pretty = false

                    if (args[0] == "pretty") {
                        pretty = true
                    }

                    // Run compiler
                    if (pretty) {
                        val result = tryCompileToString(source, true)
                        event.channel.sendMessage("This is how I understand that piece of code:\n```$result```").queue()

                    } else {
                        val result = tryCompileToString(source, false)
                        event.channel.sendMessage("```$result```").queue()
                    }

                    ErrorLog.reset()
                }
            }
        }
    }
}

fun tryCompileToString(source: String, pretty: Boolean = false): String {
    val baos = ByteArrayOutputStream()
    PrintStream(baos, true, "UTF-8").use {
            out -> tryCompile(CharStreams.fromString(source), out, pretty)
    }
    return String(baos.toByteArray())
}

fun tryCompile(source: CharStream, out: PrintStream = System.out, pretty: Boolean = false) {
    try {
        compile(source, out, pretty)

    } catch (e: TerminatedCompilationException) {

        out.println("Compilation failed!")
        out.println("Printing errors (${ErrorLog.allErrors().size}) and warnings (${ErrorLog.allWarnings().size}) ...")
        ErrorLog.printAllErrors()
        ErrorLog.allErrors()

    } catch (e: RuntimeException) {

        out.println("Compiler crashed!")
        out.println("Printing errors (${ErrorLog.allErrors().size}) and warnings (${ErrorLog.allWarnings().size}) ...")
        ErrorLog.printAllErrors()
        ErrorLog.allErrors()
        e.printStackTrace()
    }
}

fun compile(source: CharStream, out: PrintStream, pretty: Boolean) {

    // Syntactical analysis
    val lexer = DostLexer(source)
    val tokenStream = CommonTokenStream(lexer)
    val parser = DostParser(tokenStream)
    val ast = BuildAstVisitor().visit(parser.start())
    ErrorLog.assertNoErrors()

    if (pretty) {
        PrettyPrinter.print(ast, out)
        return
    }

    // Contextual analysis
    ContextualAnalysisVisitor.analyse(ast)
    ErrorLog.assertNoErrors()

    // Interpret
    Interpreter(out).start(ast)
}
