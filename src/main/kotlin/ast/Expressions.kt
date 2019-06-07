package dk.eastvillage.dost.ast

import dk.eastvillage.dost.*
import dk.eastvillage.dost.contextual.*


class Identifier(
    sctx: SourceContext?,
    var spelling: String
) : Expr(sctx, UncheckedType)

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

class ArrayLiteral(
    sctx: SourceContext?,
    var sizeExpr: Expr,
    var declaredType: Type
) : Expr(sctx, ArrayType(UncheckedType))

class BinaryExpr(
    sctx: SourceContext?,
    var left: Expr,
    var right: Expr,
    val operator: Operator
) : Expr(sctx, UncheckedType)

class NotExpr(
    sctx: SourceContext?,
    var expr: Expr
) : Expr(sctx, BoolType)

class Negation(
    sctx: SourceContext?,
    var expr: Expr
) : Expr(sctx, UncheckedType)

// -------------- Conversion expressions

class IntToFloatConversion(
    var expr: Expr
) : Expr(expr.sctx, FloatType)

class AnyToStringConversion(
    var expr: Expr
) : Expr(expr.sctx, StringType)