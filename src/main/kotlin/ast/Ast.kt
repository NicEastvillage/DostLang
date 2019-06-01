package dk.eastvillage.dost.ast

import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.contextual.Type

abstract class Node(
    val sctx: SourceContext?
)

abstract class Stmt(
    ctx: SourceContext?
) : Node(ctx)

abstract class Expr(
    sctx: SourceContext?,
    var type: Type,
    var parenthesized: Boolean = false
) : Node(sctx)

class GlobalBlock(
    sctx: SourceContext?,
    val stmts: MutableList<Stmt>
) : Node(sctx)