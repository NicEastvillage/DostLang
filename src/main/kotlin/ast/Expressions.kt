package dk.eastvillage.dost.ast

import dk.eastvillage.dost.*


class Identifier(
    ctx: SourceContext?,
    var spelling: String,
    type: Type = UncheckedType
) : Expr(ctx, type)

class IntLiteral(
    ctx: SourceContext?,
    var value: Int,
    type: Type = IntegerType
) : Expr(ctx, type)

class FloatLiteral(
    ctx: SourceContext?,
    var value: Float,
    type: Type = FloatType
) : Expr(ctx, type)

class BoolLiteral(
    ctx: SourceContext?,
    var value: Boolean,
    type: Type = BoolType
) : Expr(ctx, type)

class BinaryExpr(
    ctx: SourceContext?,
    var left: Node,
    var right: Node,
    val operator: Operator,
    type: Type = UncheckedType
) : Expr(ctx, type)

interface Operator
enum class ArithmeticOperator : Operator {
    ADD, SUB, MUL, DIV, MOD
}
enum class EqualityOperator : Operator {
    EQ, NEQ
}
enum class ComparisonOperator : Operator {
    GT, LT, LEQ, GEQ
}
enum class LogicOperator : Operator {
    AND, OR
}

class NotExpr(
    ctx: SourceContext?,
    var expr: Expr,
    type: Type = BoolType
) : Expr(ctx, type)

class Negation(
    ctx: SourceContext?,
    var expr: Expr,
    type: Type = UncheckedType
) : Expr(ctx, type)