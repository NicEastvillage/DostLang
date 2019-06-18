package dk.eastvillage.dost.ast

import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.contextual.Type
import dk.eastvillage.dost.contextual.UncheckedType


class StmtBlock(
    sctx: SourceContext?,
    val stmts: MutableList<Stmt>
) : Stmt(sctx)

class VariableDecl(
    sctx: SourceContext?,
    var variable: Identifier,
    var expr: Expr,
    var inferredType: Type = UncheckedType
) : Stmt(sctx)

class Assignment(
    sctx: SourceContext?,
    var lvalue: LValue,
    var expr: Expr
) : Stmt(sctx)

class IfStmt(
    sctx: SourceContext?,
    var condition: Expr,
    var trueBlock: Stmt,
    var falseBlock: Stmt?
) : Stmt(sctx)

class WhileLoop(
    sctx: SourceContext?,
    var condition: Expr,
    var block: StmtBlock
) : Stmt(sctx)

class ForLoop(
    sctx: SourceContext?,
    var variable: Identifier,
    var initExpr: Expr,
    var endExpr: Expr,
    var stepDirection: ForLoopStepDirection,
    var block: StmtBlock
) : Stmt(sctx)

enum class ForLoopStepDirection(val spelling: String) {
    UP_TO(".."),
    UP_TO_INC("..="),
    DOWN_TO("``"),
    DOWN_TO_INC("``=")
}

class PrintStmt(
    sctx: SourceContext?,
    var expr: Expr
) : Stmt(sctx)