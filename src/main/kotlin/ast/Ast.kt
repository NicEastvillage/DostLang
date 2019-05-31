package dk.eastvillage.dost.ast

import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.Type

abstract class Node(
    val ctx: SourceContext?
)

abstract class Stmt(
    ctx: SourceContext?
) : Node(ctx)

abstract class Expr(
    ctx: SourceContext?,
    var type: Type,
    var parenthesized: Boolean = false
) : Node(ctx)

class GlobalBlock(
    ctx: SourceContext?,
    val stmts: MutableList<Stmt>
) : Node(ctx)