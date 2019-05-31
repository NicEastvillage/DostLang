package dk.eastvillage.dost.ast

import dk.eastvillage.dost.*
import dk.eastvillage.dost.antlr.DostBaseVisitor
import dk.eastvillage.dost.antlr.DostParser
import org.antlr.v4.runtime.tree.TerminalNode
import java.lang.AssertionError


class BuildAstVisitor : DostBaseVisitor<Node>() {

    private fun collectStmts(ctx: DostParser.StmtsContext, list: MutableList<Stmt> = mutableListOf()): MutableList<Stmt> {
        return when (ctx) {
            is DostParser.CompoundStmtContext -> {
                val stmt = ctx.stmt().accept(this) as Stmt
                list.add(stmt)
                ctx.stmts()?.let { collectStmts(it, list) } ?: list
            }
            is DostParser.BlankStmtContext -> collectStmts(ctx.stmts(), list)
            is DostParser.NoStmtContext -> list
            else -> throw AssertionError("Does not know how to collect statements in ${ctx.javaClass}")
        }
    }

    override fun visitStmts_block(ctx: DostParser.Stmts_blockContext?): Node {
        return StmtBlock(
            SourceContext(ctx!!),
            collectStmts(ctx.stmts())
        )
    }

    override fun visitStart(ctx: DostParser.StartContext?): Node {
        return StmtBlock(
            SourceContext(ctx!!),
            collectStmts(ctx.stmts())
        )
    }

    override fun visitCompoundStmt(ctx: DostParser.CompoundStmtContext?): Node {
        throw AssertionError("Should not be visited.")
    }

    override fun visitBlankStmt(ctx: DostParser.BlankStmtContext?): Node {
        throw AssertionError("Should not be visited.")
    }

    override fun visitNoStmt(ctx: DostParser.NoStmtContext?): Node {
        throw AssertionError("Should not be visited.")
    }

    override fun visitStmt_end(ctx: DostParser.Stmt_endContext?): Node {
        throw AssertionError("Should not be visited.")
    }

    override fun visitVariableDecl(ctx: DostParser.VariableDeclContext?): Node {
        return VariableDecl(
            SourceContext(ctx!!),
            getType(ctx.type()),
            identFrom(ctx.IDENT()),
            ctx.expr().accept(this) as Expr
        )
    }

    private fun getType(type: DostParser.TypeContext): Type {
        return when {
            type.INT() != null -> IntegerType
            type.FLOAT() != null -> FloatType
            type.BOOL() != null -> BoolType
            else -> throw AssertionError("Unknown type.")
        }
    }

    override fun visitAssignment(ctx: DostParser.AssignmentContext?): Node {
        return Assignment(
            SourceContext(ctx!!),
            identFrom(ctx.IDENT()),
            ctx.expr().accept(this) as Expr
        )
    }

    override fun visitIfStmt(ctx: DostParser.IfStmtContext?): Node {
        return ctx!!.if_stmt().accept(this)
    }

    override fun visitIf_stmt(ctx: DostParser.If_stmtContext?): Node {
        return IfStmt(
            SourceContext(ctx!!),
            ctx.expr().accept(this) as Expr,
            ctx.stmts_block().accept(this) as Stmt,
            ctx.if_end()?.accept(this) as Stmt?
        )
    }

    override fun visitIf_end(ctx: DostParser.If_endContext?): Node {
        return when {
            ctx!!.if_stmt() != null -> ctx.if_stmt().accept(this)
            else -> ctx.stmts_block().accept(this)
        }
    }

    override fun visitWhileLoop(ctx: DostParser.WhileLoopContext?): Node {
        return WhileLoop(
            SourceContext(ctx!!),
            ctx.expr().accept(this) as Expr,
            ctx.stmts_block().accept(this) as StmtBlock
        )
    }

    override fun visitForLoop(ctx: DostParser.ForLoopContext?): Node {
        return ctx!!.for_loop().accept(this)
    }

    override fun visitFor_loop(ctx: DostParser.For_loopContext?): Node {
        return ForLoop(
            SourceContext(ctx!!),
            identFrom(ctx.IDENT()),
            ctx.init.accept(this) as Expr,
            ctx.end.accept(this) as Expr,
            getStepDirection(ctx.range()),
            ctx.stmts_block().accept(this) as StmtBlock
        )
    }

    private fun getStepDirection(range: DostParser.RangeContext): ForLoopStepDirection {
        return when {
            range.UPTO() != null -> ForLoopStepDirection.UP_TO
            range.UPTOINC() != null -> ForLoopStepDirection.UP_TO_INC
            range.DOWNTO() != null -> ForLoopStepDirection.DOWN_TO
            range.DOWNTOINC() != null -> ForLoopStepDirection.DOWN_TO_INC
            else -> throw AssertionError("Unknown step direction for for-loop.")
        }
    }

    override fun visitRange(ctx: DostParser.RangeContext?): Node {
        throw AssertionError("Should not be visited.")

    }

    override fun visitInequalityExpr(ctx: DostParser.InequalityExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.NEQ().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.NEQ
        )
    }

    override fun visitNegationExpr(ctx: DostParser.NegationExprContext?): Node {
        return Negation(
            SourceContext(ctx!!.SUB().symbol),
            ctx.expr().accept(this) as Expr
        )
    }

    override fun visitModuloExpr(ctx: DostParser.ModuloExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.MOD().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.MOD
        )
    }

    override fun visitGreaterOrEqExpr(ctx: DostParser.GreaterOrEqExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.GEQ().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.GEQ
        )
    }

    override fun visitAdditionExpr(ctx: DostParser.AdditionExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.ADD().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.ADD
        )
    }

    override fun visitLessThanExpr(ctx: DostParser.LessThanExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.LT().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.LT
        )
    }

    override fun visitOrExpr(ctx: DostParser.OrExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.OR().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.OR
        )
    }

    override fun visitParenExpr(ctx: DostParser.ParenExprContext?): Node {
        return ctx!!.expr().accept(this)
    }

    override fun visitDivisionExpr(ctx: DostParser.DivisionExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.DIV().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.DIV
        )
    }

    override fun visitIdentExpr(ctx: DostParser.IdentExprContext?): Node {
        return identFrom(ctx!!.IDENT())
    }

    private fun identFrom(id: TerminalNode): Identifier {
        return Identifier(
            SourceContext(id.symbol),
            id.text
        )
    }

    override fun visitGreaterThanExpr(ctx: DostParser.GreaterThanExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.GT().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.GT
        )
    }

    override fun visitNotExpr(ctx: DostParser.NotExprContext?): Node {
        return NotExpr(
            SourceContext(ctx!!.BANG().symbol),
            ctx.expr().accept(this) as Expr
        )
    }

    override fun visitLiteralExpr(ctx: DostParser.LiteralExprContext?): Node {
        return ctx!!.literal().accept(this)
    }

    override fun visitMultiplictionExpr(ctx: DostParser.MultiplictionExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.MUL().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.MUL
        )
    }

    override fun visitSubstractionExpr(ctx: DostParser.SubstractionExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.SUB().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.SUB
        )
    }

    override fun visitLessOrEqExpr(ctx: DostParser.LessOrEqExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.LEQ().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.LEQ
        )
    }

    override fun visitEqualityExpr(ctx: DostParser.EqualityExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.EQ().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.EQ
        )
    }

    override fun visitAndExpr(ctx: DostParser.AndExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.AND().symbol),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.AND
        )
    }

    override fun visitFun_call(ctx: DostParser.Fun_callContext?): Node {
        TODO("not implemented")
    }

    override fun visitLiteral(ctx: DostParser.LiteralContext?): Node {
        return when {
            ctx!!.INUM() != null -> IntLiteral(SourceContext(ctx), ctx.INUM().text.toInt())
            ctx.FNUM() != null -> FloatLiteral(SourceContext(ctx), ctx.INUM().text.toFloat())
            ctx.TRUE() != null -> BoolLiteral(SourceContext(ctx), true)
            ctx.FALSE() != null -> BoolLiteral(SourceContext(ctx), false)
            else -> throw AssertionError("Unknown literal.")
        }
    }

    override fun visitType(ctx: DostParser.TypeContext?): Node {
        throw AssertionError("Should not be visited.")
    }
}