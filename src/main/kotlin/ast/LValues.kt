package dk.eastvillage.dost.ast

import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.contextual.UncheckedType


open class LValue(
    sctx: SourceContext?
) : Expr(sctx, UncheckedType)

class LValueVariable(
    sctx: SourceContext?,
    var variable: Identifier
) : LValue(sctx)

class LValueIndexing(
    sctx: SourceContext?,
    var lvalue: LValue,
    var expr: Expr
) : LValue(sctx)