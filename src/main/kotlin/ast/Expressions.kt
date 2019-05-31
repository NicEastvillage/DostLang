package dk.eastvillage.dost.ast

import dk.eastvillage.dost.*


class Identifier(
    sctx: SourceContext?,
    var spelling: String,
    type: Type = UncheckedType
) : Expr(sctx, type)

class IntLiteral(
    sctx: SourceContext?,
    var value: Int,
    type: Type = IntegerType
) : Expr(sctx, type)

class FloatLiteral(
    sctx: SourceContext?,
    var value: Float,
    type: Type = FloatType
) : Expr(sctx, type)

class BoolLiteral(
    sctx: SourceContext?,
    var value: Boolean,
    type: Type = BoolType
) : Expr(sctx, type)

class BinaryExpr(
    sctx: SourceContext?,
    var left: Node,
    var right: Node,
    val operator: Operator,
    type: Type = UncheckedType
) : Expr(sctx, type)

class NotExpr(
    sctx: SourceContext?,
    var expr: Expr,
    type: Type = BoolType
) : Expr(sctx, type)

class Negation(
    sctx: SourceContext?,
    var expr: Expr,
    type: Type = UncheckedType
) : Expr(sctx, type)