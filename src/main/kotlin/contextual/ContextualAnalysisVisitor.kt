package dk.eastvillage.dost.contextual

import dk.eastvillage.dost.CompileError
import dk.eastvillage.dost.ErrorLog
import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.ast.*


class UndeclaredIdentError(sctx: SourceContext?, ident: String) :
    CompileError(sctx, "'$ident' has not been declared.")

class NotReassignableError(sctx: SourceContext?, ident: String, decl: Node) :
    CompileError(sctx, "'$ident' is not assignable since it was declared as a ${decl.javaClass}.")


@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object ContextualAnalysisVisitor : BaseVisitor<ContextualAnalysisVisitor.Context, Unit>(Unit) {

    class Context(
        val identTable: IdentTable = IdentTable()
    )

    fun analyse(node: Node) {
        val ctx = Context()
        visit(node, ctx)
    }

    override fun visit(node: StmtBlock, ctx: Context) {
        ctx.identTable.openScope()
        for (stmt in node.stmts) {
            visit(stmt, ctx)
        }
        ctx.identTable.closeScope()
    }

    override fun visit(node: VariableDecl, ctx: Context) {
        visit(node.expr, ctx)
        ctx.identTable[node.variable.spelling] = node //TODO Redeclaration fail points to type, not identifier
    }

    override fun visit(node: Assignment, ctx: Context) {
        visit(node.expr, ctx)
        val decl = ctx.identTable[node.variable.spelling]
        when (decl) {
            null -> ErrorLog += UndeclaredIdentError(node.variable.sctx, node.variable.spelling)
            is VariableDecl -> { // ok TODO: Type checking
            }
            else -> ErrorLog += NotReassignableError(node.variable.sctx, node.variable.spelling, decl)
        }
    }

    override fun visit(node: IfStmt, ctx: Context) {
        visit(node.condition, ctx)
        visit(node.trueBlock, ctx)
        node.falseBlock?.let { visit(it, ctx) }
    }

    override fun visit(node: ForLoop, ctx: Context) {
        visit(node.initExpr, ctx)
        visit(node.endExpr, ctx)
        ctx.identTable.openScope()
        ctx.identTable[node.variable.spelling] = node
        visit(node.block, ctx)
        ctx.identTable.closeScope()
    }

    override fun visit(node: WhileLoop, ctx: Context) {
        visit(node.condition, ctx)
        visit(node.block, ctx)
    }

    override fun visit(node: Identifier, ctx: Context) {
        val decl = ctx.identTable[node.spelling]
        when (decl) {
            null -> ErrorLog += UndeclaredIdentError(node.sctx, node.spelling)
        }
    }

    override fun visit(node: BinaryExpr, ctx: Context) {
        visit(node.left, ctx)
        visit(node.right, ctx)
    }

    override fun visit(node: NotExpr, ctx: Context) {
        visit(node.expr, ctx)
    }

    override fun visit(node: Negation, ctx: Context) {
        visit(node.expr, ctx)
    }
}