package dk.eastvillage.dost

import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.antlr.v4.runtime.CharStreams
import java.io.ByteArrayOutputStream
import java.io.PrintStream


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Needs one argument ('bot-token'), or two ('test [pretty]')")
    } else {
        if (args[0] == "test") {
            val source = """
                |var a = 4.0
                |var b = a * a / 2
                |var c = false
                |if (b > a) {
                |   c = true
                |   a = 2.234
                |   var a = "It was true!"
                |   a = "new string!"
                |   print a
                |}
                |else {
                |   print "c is " + c
                |}
                |print "A: " + a
            """.trimMargin()
            val pretty = args.size == 2 && args[1] == "pretty"
            tryCompile(
                CompilationSettings(
                    source = CharStreams.fromString(source),
                    doPrettyPrinting = pretty
                )
            )

        } else {
            val jda = JDABuilder(args[0]).build()
            jda.addEventListener(MessageListener())
        }
    }
}

class MessageListener : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.author.isBot) {
            println("Msg received from '${event.author.name}': ${event.message.contentRaw}")
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
                    try {
                        val stdoutBaos = ByteArrayOutputStream()
                        val stderrBaos = ByteArrayOutputStream()
                        val stdoutStream = PrintStream(stdoutBaos, true, "UTF-8")
                        val stderrStream = PrintStream(stderrBaos, true, "UTF-8")

                        val info = tryCompile(
                            CompilationSettings(
                                source = CharStreams.fromString(source),
                                doPrettyPrinting = pretty,
                                stdout = stdoutStream,
                                stderr = stderrStream
                            )
                        )

                        val outString = String(stdoutBaos.toByteArray())
                        val errString = String(stderrBaos.toByteArray())

                        when (info.termination) {
                            TerminationTime.CORRECTLY -> if (pretty) {
                                event.channel.sendMessage("This is how I understand that piece of code:\n```$outString```").queue()
                            } else {
                                event.channel.sendMessage("```$outString```").queue()
                            }
                            TerminationTime.DURING_PARSING -> {
                                event.channel.sendMessage("Failed to parse that piece of code :(").queue()
                            }
                            TerminationTime.DURING_ANALYSIS -> {
                                event.channel.sendMessage("Compilation failed!\n```$errString```").queue()
                            }
                            TerminationTime.DURING_INTERPRETATION -> {
                                event.channel.sendMessage("Interpretation runtime error!\n```$errString```").queue()
                            }
                        }

                    } catch (e: Exception) {
                        System.err.print("Failed replying to message.")
                        e.printStackTrace()
                    }

                }
            }
        }
    }
}
