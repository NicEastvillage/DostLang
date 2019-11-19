package dk.eastvillage.dost.interpreter


import java.util.*
import kotlin.collections.HashMap

class UndeclaredSymbol(symbol: String) : InterpreterException("'$symbol' was undefined!")
class RedeclaredSymbol(symbol: String) : InterpreterException("'$symbol' already declared!")

class ArrayPointer(val addrToSize: Int, val addrToValues: Int)

class Memory {

    // Each memory address is used once and is never garbage collected, so only a finite number of variables can be used
    private val RAM_SIZE = 4_096

    // The actual data structures of the Memory
    // Identifiers are named pointers
    private class ValueTable : HashMap<String, Int>()
    private class NestedScope : Stack<ValueTable>()
    private val stack = Stack<NestedScope>()
    private val ram: Array<Any> = Array(RAM_SIZE) { -42424242 }
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
    fun set(identifier: String, value: Any) {
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
    fun get(identifier: String): Any {
        for (table in stack.peek().reversed()) {
            table[identifier]?.let { return ram[it] }
        }
        throw UndeclaredSymbol(identifier)
    }

    /**
     * Changes the value of the given address to the given value
     */
    operator fun set(addr: Int, value: Any) {
        ram[addr] = value
    }

    /**
     * Retrieve the value at the given address
     */
    operator fun get(addr: Int): Any {
        return ram[addr]
    }

    /**
     * Allocates multiple values in the RAM.
     * The values will be uninitialized.
     * The address of the first value is returned.
     */
    fun allocChunk(size: Int): Int {
        val addr = nextAddress
        nextAddress += size
        return addr
    }

    /**
     * Add a new identifier in the current stack/scope.
     * It refers to a chunk of uninitialized memory.
     */
    fun declareChunk(identifier: String, size: Int) {
        if (stack.peek().peek().containsKey(identifier)) {
            throw RedeclaredSymbol(identifier)
        }
        val addr = allocChunk(size)
        stack.peek().peek()[identifier] = addr
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