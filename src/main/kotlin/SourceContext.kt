package dk.eastvillage.dost

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

/**
 * The SourceContext data class contains the position of the context in the source code from which an AST node was created.
 * @param lineNumber The line number in the source program. In the range 1..n
 * @param charPositionInLine The position of the first character of the AST node in source program. In the range 0..n-1
 * @param text The context as the a string from the original source code
 */
data class SourceContext(
    val lineNumber: Int,
    val charPositionInLine: Int
) {
    constructor(ctx: ParserRuleContext) : this(ctx.start.line, ctx.start.charPositionInLine)
    constructor(token: Token) : this(token.line, token.charPositionInLine)

    /**
     * Returns a formatted string of the source position
     * @return "(line:char)"
     */
    fun position(): String {
        return "($lineNumber:$charPositionInLine)"
    }
}