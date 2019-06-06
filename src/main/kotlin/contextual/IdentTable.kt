package dk.eastvillage.dost.contextual

import dk.eastvillage.dost.CompileError
import dk.eastvillage.dost.ErrorLog
import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.ast.Node
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap


class IdentTable {

    private val tableStack: Stack<HashMap<String, Node>> = Stack()

    init {
        openScope() // Standard environment
    }

    fun openScope() {
        tableStack.push(HashMap())
    }

    fun closeScope() {
        tableStack.pop()
    }

    operator fun set(ident: String, decl: Node) {
        val table = tableStack.peek()
        if (table.contains(ident)) {
            throw RedeclarationException(RedeclarationError(decl.sctx, ident))
        } else {
            table[ident] = decl
        }
    }

    operator fun get(ident: String): Node? {
        for (table in tableStack.reversed()) {
            table[ident]?.let { return it }
        }
        return null
    }
}

class RedeclarationError(sctx: SourceContext?, ident: String) : CompileError(sctx, "'$ident' has already been declared.")
class RedeclarationException(val error: RedeclarationError) : RuntimeException(error.message)
