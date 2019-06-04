package dk.eastvillage.dost

import dk.eastvillage.dost.ast.*
import java.io.PrintStream


@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class PrettyPrinter(
    private val settings: CompilationSettings
) : BaseVisitor<Unit, Unit>(Unit) {

    private val indent: String = "    "
    
    private var indentLevel: Int = 0
    
    private fun print(str: String) = settings.stdout.print(str)
    
    private fun indent() = print(indent.repeat(indentLevel))
    
    private fun incIndentLevel() = indentLevel++

    private fun decIndentLevel() =
        if (--indentLevel < 0) throw AssertionError("Indentation level became negative.") else Unit

    fun start(node: Node) {
        visit(node, Unit)
    }

    override fun visit(node: Expr, data: Unit) {
        if (node.parenthesized) {
            print("(")
            super.visit(node, Unit)
            print(")")
        } else super.visit(node, Unit)
    }

    override fun visit(node: GlobalBlock, data: Unit) {
        for (stmt in node.stmts) {
            visit(stmt, Unit)
            print("\n")
        }
    }

    override fun visit(node: StmtBlock, data: Unit) {
        print("{\n")
        incIndentLevel()
        for (stmt in node.stmts) {
            indent()
            visit(stmt, Unit)
            print("\n")
        }
        decIndentLevel()
        indent()
        print("}")
    }

    override fun visit(node: VariableDecl, data: Unit) {
        print("var ${node.variable.spelling} = ")
        visit(node.expr, Unit)
    }

    override fun visit(node: Assignment, data: Unit) {
        print("${node.variable.spelling} = ")
        visit(node.expr, Unit)
    }

    override fun visit(node: IfStmt, data: Unit) {
        print("if (")
        visit(node.condition, Unit)
        print(") ")
        visit(node.trueBlock, Unit)
        if (node.falseBlock != null) {
            print(" else ")
            visit(node.falseBlock as Stmt, Unit)
        }
    }

    override fun visit(node: ForLoop, data: Unit) {
        print("for (")
        visit(node.variable, Unit)
        print(" in ")
        visit(node.initExpr, Unit)
        print(" ${node.stepDirection.spelling} ")
        visit(node.endExpr, Unit)
        print(") ")
        visit(node.block, Unit)
    }

    override fun visit(node: WhileLoop, data: Unit) {
        print("while (")
        visit(node.condition, Unit)
        print(") ")
        visit(node.block, Unit)
    }

    override fun visit(node: PrintStmt, data: Unit) {
        print("print ")
        visit(node.expr, Unit)
    }

    override fun visit(node: Identifier, data: Unit) {
        print(node.spelling)
    }

    override fun visit(node: IntLiteral, data: Unit) {
        print(node.value)
    }

    override fun visit(node: FloatLiteral, data: Unit) {
        print(node.value)
    }

    override fun visit(node: BoolLiteral, data: Unit) {
        print(node.value)
    }

    override fun visit(node: StringLiteral, data: Unit) {
        print("\"${node.value}\"")
    }

    override fun visit(node: BinaryExpr, data: Unit) {
        visit(node.left, Unit)
        print(" ${node.operator.spelling} ")
        visit(node.right, Unit)
    }

    override fun visit(node: NotExpr, data: Unit) {
        print("!")
        visit(node.expr, Unit)
    }

    override fun visit(node: Negation, data: Unit) {
        print("-")
        visit(node.expr, Unit)
    }

    override fun visit(node: IntToFloatConversion, data: Unit) {
        visit(node, Unit)
    }

    override fun visit(node: AnyToStringConversion, data: Unit) {
        visit(node, Unit)
    }
}