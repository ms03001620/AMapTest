package com.polestar.charging.ui.cluster.view

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingDeque

class ViewModifierKotlin(
    val coroutineScope: CoroutineScope
) {
    interface WorkTask {
        suspend fun work(function: () -> Unit)
    }

    private val deque = LinkedBlockingDeque<WorkTask>(MAX)
    private var isRunning = false

    fun working() {
        if (isRunning) {
            return
        }

        isRunning = true

        coroutineScope.launch {
            deque.peek()?.work {
                deque.take()
                isRunning = false
                if (deque.isNotEmpty()) {
                    working()
                }
            }
        }
    }

    fun appendTaskToTail(t: WorkTask) {
        if (deque.size == MAX) {
            deque.removeLast()
        }
        deque.add(t)
        working()
    }

    companion object {
        const val MAX = 2
    }
}