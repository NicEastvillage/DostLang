package dk.eastvillage.dost.interpreter


import java.util.*
import kotlin.collections.HashMap

class UndeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' was undefined!")
class RedeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' already declared!")


class Memory {

    // Each memory address is used once and is never garbage collected, so only a finite number of variables can be used
    private val RAM_SIZE = 4_096

    // The actual data structures of the Memory
    private class ValueTable : HashMap<String, Int>()
    private class NestedScope : Stack<ValueTable>()
    private val stack = Stack<NestedScope>()
    private val ram: Array<Any?> = Array(RAM_SIZE) { null }
    private var nextAddress = 0

    /**
     * Add a new identifier in the current stack/scope
     */
    fun declare(identifier: String, value: Any) {
        if (stack.peek().peek().containsKey(identifier)) {
            throw RedeclaredSymbol(identifier)
        }
        val addr = nextAddress++
        stack.peek().peek()[identifier] = addr
        ram[addr] = value
    }

    /**
     * Change the value associated with the given identifier
     */
    operator fun set(identifier: String, value: Any) {
        for (table in stack.peek().reversed()) {
            table[identifier]?.let {
                ram[it] = value
                return
            }
        }
        throw UndeclaredSymbol(identifier)
    }

    /**
     * Retrieve the value associated with the given identifier
     */
    operator fun get(identifier: String): Any {
        for (table in stack.peek().reversed()) {
            table[identifier]?.let { return ram[it]!! }
        }
        throw UndeclaredSymbol(identifier)
    }

    fun pushStack() {
        stack.push(NestedScope())
    }

    fun popStack() {
        stack.pop()
    }

    fun openScope() {
        stack.peek().push(ValueTable())
    }

    fun closeScope() {
        stack.peek().pop()
    }
}