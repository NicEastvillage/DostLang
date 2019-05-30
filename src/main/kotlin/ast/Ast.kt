package dk.eastvillage.dost.ast

import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.Type

abstract class Node

abstract class NodeWithCtx(
    val ctx: SourceContext
) : Node()

abstract class Stmt(
    ctx: SourceContext
) : NodeWithCtx(ctx)

abstract class Expr(
    ctx: SourceContext,
    var type: Type
) : NodeWithCtx(ctx)