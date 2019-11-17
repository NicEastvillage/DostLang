package dk.eastvillage.dost.ast

import dk.eastvillage.dost.*
import dk.eastvillage.dost.antlr.DostBaseVisitor
import dk.eastvillage.dost.antlr.DostParser
import dk.eastvillage.dost.contextual.*
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

    override fun visitStart(ctx: DostParser.StartContext?): Node {
        return GlobalBlock(
            SourceContext(ctx!!),
            collectStmts(ctx.stmts())
        )
    }

    override fun visitStmts_block(ctx: DostParser.Stmts_blockContext?): Node {
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
            identFrom(ctx.IDENT()),
            ctx.expr().accept(this) as Expr
        )
    }

    override fun visitAssignment(ctx: DostParser.AssignmentContext?): Node {
        return Assignment(
            SourceContext(ctx!!),
            ctx.lvalue().accept(this) as LValue,
            ctx.expr().accept(this) as Expr
        )
    }

    override fun visitLvalue(ctx: DostParser.LvalueContext?): Node {
        return when {
            ctx!!.IDENT() != null -> LValueVariable(SourceContext(ctx), identFrom(ctx.IDENT()))
            else -> LValueIndexing(
                SourceContext(ctx.LBRACK()),
                ctx.lvalue().accept(this) as LValue,
                ctx.index.accept(this) as Expr
            )
        }
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
            else -> throw AssertionError("Unknown step direction for for-loop. $range")
        }
    }

    override fun visitRange(ctx: DostParser.RangeContext?): Node {
        throw AssertionError("Should not be visited.")

    }

    override fun visitPrintStmt(ctx: DostParser.PrintStmtContext?): Node {
        return PrintStmt(
            SourceContext(ctx!!),
            ctx.expr().accept(this) as Expr
        )
    }

    override fun visitInequalityExpr(ctx: DostParser.InequalityExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.NEQ()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.NEQ
        )
    }

    override fun visitNegationExpr(ctx: DostParser.NegationExprContext?): Node {
        return Negation(
            SourceContext(ctx!!.SUB()),
            ctx.expr().accept(this) as Expr
        )
    }

    override fun visitModuloExpr(ctx: DostParser.ModuloExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.MOD()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.MOD
        )
    }

    override fun visitGreaterOrEqExpr(ctx: DostParser.GreaterOrEqExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.GEQ()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.GEQ
        )
    }

    override fun visitAdditionExpr(ctx: DostParser.AdditionExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.ADD()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.ADD
        )
    }

    override fun visitLessThanExpr(ctx: DostParser.LessThanExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.LT()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.LT
        )
    }

    override fun visitOrExpr(ctx: DostParser.OrExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.OR()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.OR
        )
    }

    override fun visitParenExpr(ctx: DostParser.ParenExprContext?): Node {
        val expr = ctx!!.expr().accept(this) as Expr
        expr.parenthesized = true
        return expr
    }

    override fun visitDivisionExpr(ctx: DostParser.DivisionExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.DIV()),
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
            SourceContext(id),
            id.text
        )
    }

    override fun visitGreaterThanExpr(ctx: DostParser.GreaterThanExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.GT()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.GT
        )
    }

    override fun visitNotExpr(ctx: DostParser.NotExprContext?): Node {
        return NotExpr(
            SourceContext(ctx!!.BANG()),
            ctx.expr().accept(this) as Expr
        )
    }

    override fun visitLiteralExpr(ctx: DostParser.LiteralExprContext?): Node {
        return ctx!!.literal().accept(this)
    }

    override fun visitMultiplictionExpr(ctx: DostParser.MultiplictionExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.MUL()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.MUL
        )
    }

    override fun visitSubstractionExpr(ctx: DostParser.SubstractionExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.SUB()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.SUB
        )
    }

    override fun visitLessOrEqExpr(ctx: DostParser.LessOrEqExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.LEQ()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.LEQ
        )
    }

    override fun visitEqualityExpr(ctx: DostParser.EqualityExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.EQ()),
            ctx.left.accept(this) as Expr,
            ctx.right.accept(this) as Expr,
            Operators.EQ
        )
    }

    override fun visitAndExpr(ctx: DostParser.AndExprContext?): Node {
        return BinaryExpr(
            SourceContext(ctx!!.AND()),
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
            ctx!!.array_lit() != null -> ctx.array_lit().accept(this)
            ctx.INUM() != null -> IntLiteral(SourceContext(ctx), ctx.INUM().text.toInt())
            ctx.FNUM() != null -> FloatLiteral(SourceContext(ctx), ctx.FNUM().text.toFloat())
            ctx.TRUE() != null -> BoolLiteral(SourceContext(ctx), true)
            ctx.FALSE() != null -> BoolLiteral(SourceContext(ctx), false)
            ctx.STRING() != null -> StringLiteral(SourceContext(ctx), ctx.text.drop(1).dropLast(1))
            else -> throw AssertionError("Unknown literal.")
        }
    }

    override fun visitArray_lit(ctx: DostParser.Array_litContext?): Node {
        return ArrayLiteral(
            SourceContext(ctx!!),
            ctx.expr().accept(this) as Expr,
            getType(ctx.type())
        )
    }

    private fun getType(ctx: DostParser.TypeContext): Type {
        return when (ctx) {
            is DostParser.SimpleTypeContext -> when (ctx.IDENT().text) {
                "int" -> IntegerType
                "float" -> FloatType
                "bool" -> BoolType
                "string" -> StringType
                else -> throw AssertionError("Unknown custom type.")
            }
            is DostParser.ArrayTypeContext -> ArrayType(getType(ctx.subtype))
            else -> throw AssertionError("Unknown type.")
        }
    }

    override fun visitSimpleType(ctx: DostParser.SimpleTypeContext?): Node {
        return super.visitSimpleType(ctx)
    }

    override fun visitArrayType(ctx: DostParser.ArrayTypeContext?): Node {
        return super.visitArrayType(ctx)
    }
}