package dk.eastvillage.dost.ast

import dk.eastvillage.dost.*


class Identifier(
    ctx: SourceContext,
    var spelling: String,
    type: Type = UncheckedType
) : Expr(ctx, type)

class IntLiteral(
    ctx: SourceContext,
    var value: Int,
    type: Type = IntegerType
) : Expr(ctx, type)

class FloatLiteral(
    ctx: SourceContext,
    var value: Float,
    type: Type = FloatType
) : Expr(ctx, type)

class BoolLiteral(
    ctx: SourceContext,
    var value: Boolean,
    type: Type = BoolType
) : Expr(ctx, type)

class BinaryExpr(
    ctx: SourceContext,
    var left: Node,
    var right: Node,
    val operator: Operator,
    type: Type = UncheckedType
) : Expr(ctx, type)

sealed class Operator(val leftToRightAssociative: Boolean)
sealed class ArithmeticOperator(leftToRightAssociative: Boolean) : Operator(leftToRightAssociative)
sealed class EqualityOperator(leftToRightAssociative: Boolean) : Operator(leftToRightAssociative)
sealed class ComparisonOperator(leftToRightAssociative: Boolean) : Operator(leftToRightAssociative)
sealed class LogicOperator(leftToRightAssociative: Boolean) : Operator(leftToRightAssociative)

object ADD : ArithmeticOperator(true)
object SUB : ArithmeticOperator(true)
object MUL : ArithmeticOperator(true)
object DIV : ArithmeticOperator(true)
object MOD : ArithmeticOperator(true)

object GT : ComparisonOperator(true)
object LT : ComparisonOperator(true)
object LEQ : ComparisonOperator(true)
object GEQ : ComparisonOperator(true)

object EQ : EqualityOperator(true)
object NEQ : EqualityOperator(true)

object AND : LogicOperator(true)
object OR : LogicOperator(true)

class NotExpr(
    ctx: SourceContext,
    var expr: Expr,
    type: Type = BoolType
) : Expr(ctx, type)

class Negation(
    ctx: SourceContext,
    var expr: Expr,
    type: Type = UncheckedType
) : Expr(ctx, type)