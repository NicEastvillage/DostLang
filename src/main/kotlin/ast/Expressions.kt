package dk.eastvillage.dost.ast

import dk.eastvillage.dost.*
import dk.eastvillage.dost.contextual.*


class Identifier(
    sctx: SourceContext?,
    var spelling: String,
    type: Type = UncheckedType
) : Expr(sctx, type)

class IntLiteral(
    sctx: SourceContext?,
    var value: Int
) : Expr(sctx, IntegerType)

class FloatLiteral(
    sctx: SourceContext?,
    var value: Float
) : Expr(sctx, FloatType)

class BoolLiteral(
    sctx: SourceContext?,
    var value: Boolean
) : Expr(sctx, BoolType)

class StringLiteral(
    sctx: SourceContext?,
    var value: String
) : Expr(sctx, StringType)

class BinaryExpr(
    sctx: SourceContext?,
    var left: Expr,
    var right: Expr,
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

// -------------- Conversion expressions

class IntToFloatConversion(
    var expr: Expr
) : Expr(expr.sctx, FloatType)

class AnyToStringConversion(
    var expr: Expr
) : Expr(expr.sctx, StringType)