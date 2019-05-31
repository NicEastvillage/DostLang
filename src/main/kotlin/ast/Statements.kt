package dk.eastvillage.dost.ast

import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.Type


class StmtBlock(
    ctx: SourceContext?,
    val stmts: MutableList<Stmt>
) : Stmt(ctx)

class VariableDecl(
    ctx: SourceContext?,
    var declType: Type,
    var variable: Identifier,
    var expr: Expr
) : Stmt(ctx)

class Assignment(
    ctx: SourceContext?,
    var variable: Identifier,
    var expr: Expr
) : Stmt(ctx)

class IfStmt(
    ctx: SourceContext?,
    var condition: Expr,
    var trueBlock: Stmt,
    var falseBlock: Stmt?
) : Stmt(ctx)

class WhileLoop(
    ctx: SourceContext?,
    var condition: Expr,
    var block: StmtBlock
) : Stmt(ctx)

class ForLoop(
    ctx: SourceContext?,
    var variable: Identifier,
    var initExpr: Expr,
    var endExpr: Expr,
    var stepDirection: ForLoopStepDirection,
    var block: StmtBlock
) : Stmt(ctx)

enum class ForLoopStepDirection {
    UP_TO, UP_TO_INC, DOWN_TO, DOWN_TO_INC
}