package dk.eastvillage.dost.ast

import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.Type

abstract class Node(
    val ctx: SourceContext
)

abstract class Stmt(
    ctx: SourceContext
) : Node(ctx)

abstract class Expr(
    ctx: SourceContext,
    type: Type
) : Node(ctx)