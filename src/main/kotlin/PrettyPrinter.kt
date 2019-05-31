package dk.eastvillage.dost

import dk.eastvillage.dost.ast.*
import java.io.PrintStream


@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class PrettyPrinter : DostBaseVisitor<PrettyPrinter.PrintInfo, Unit>(Unit) {

    class PrintInfo(
        val out: PrintStream,
        private var indentLevel: Int = 0,
        val indent: String = "    "
    ) {
        fun printIndent() = out.print(indent.repeat(indentLevel))
        fun incIndentLevel() = indentLevel++
        fun decIndentLevel() =
            if (--indentLevel < 0) throw AssertionError("Indentation level became negative.") else Unit
    }

    fun print(node: Node, out: PrintStream, indent: String = "    ") {
        val print = PrintInfo(out, indent = indent)
        visit(node, print)
    }

    override fun visit(node: Expr, print: PrintInfo) {
        if (node.parenthesized) {
            print.out.print("(")
            super.visit(node, print)
            print.out.print(")")
        } else super.visit(node, print)
    }

    override fun visit(node: StmtBlock, print: PrintInfo) {
        print.out.print("{\n")
        print.incIndentLevel()
        for (stmt in node.stmts) {
            print.printIndent()
            visit(stmt, print)
            print.out.print("\n")
        }
        print.decIndentLevel()
        print.printIndent()
        print.out.print("}")
    }

    override fun visit(node: VariableDecl, print: PrintInfo) {
        print.out.print("${node.declType.name} ${node.variable.spelling} = ")
        visit(node.expr, print)
    }

    override fun visit(node: Assignment, print: PrintInfo) {
        print.out.print("${node.variable.spelling} = ")
        visit(node.expr, print)
    }

    override fun visit(node: IfStmt, print: PrintInfo) {
        print.out.print("if (")
        visit(node.condition, print)
        print.out.print(") ")
        visit(node.trueBlock, print)
        if (node.falseBlock != null) {
            print.out.print(" else ")
            visit(node.falseBlock as Stmt, print)
        }
    }

    override fun visit(node: ForLoop, print: PrintInfo) {
        print.out.print("for (")
        visit(node.variable, print)
        print.out.print(" : ")
        visit(node.initExpr, print)
        print.out.print(" ${node.stepDirection.spelling} ")
        visit(node.endExpr, print)
        print.out.print(") ")
        visit(node.block, print)
    }

    override fun visit(node: WhileLoop, print: PrintInfo) {
        print.out.print("while (")
        visit(node.condition, print)
        print.out.print(") ")
        visit(node.block, print)
    }

    override fun visit(node: Identifier, print: PrintInfo) {
        print.out.print(node.spelling)
    }

    override fun visit(node: IntLiteral, print: PrintInfo) {
        print.out.print(node.value)
    }

    override fun visit(node: FloatLiteral, print: PrintInfo) {
        print.out.print(node.value)
    }

    override fun visit(node: BoolLiteral, print: PrintInfo) {
        print.out.print(node.value)
    }

    override fun visit(node: BinaryExpr, print: PrintInfo) {
        visit(node.left, print)
        print.out.print(" ${node.operator.spelling} ")
        visit(node.right, print)
    }

    override fun visit(node: NotExpr, print: PrintInfo) {
        print.out.print("!")
        visit(node.expr, print)
    }

    override fun visit(node: Negation, print: PrintInfo) {
        print.out.print("-")
        visit(node.expr, print)
    }
}