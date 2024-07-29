package org.github.aanno.pgconv.impl

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

private data class PrevNext(val prev: String, val next: String)

class SequenceFactory {

    private val queue: Queue<PrevNext> = ConcurrentLinkedQueue<PrevNext>()

    fun add(prev: String, next: String) {
        if (prev.trim().length > 0 && next.trim().length > 0) {
            queue.add(PrevNext(prev, next))
        }
    }

    fun build(): List<String> {
        val result = LinkedList<String>()
        queue.forEach {
            var prevIdx = result.indexOf(it.prev)
            if (prevIdx < 0) {
                result.add(it.prev)
                prevIdx = result.indexOf(it.prev)
            }
            val nextIdx = result.indexOf(it.next)
            if (nextIdx < 0) {
                result.add(prevIdx + 1, it.next)
            } else {
                if (prevIdx + 1 != nextIdx) throw IllegalStateException(
                    "prev: ${it.prev} (${prevIdx}) next: ${it.next} (${nextIdx}) queue: ${queue}"
                )
            }
        }
        return result
    }
}
