package com.example.domain

/** Remaining whole seconds from a monotonic deadline, rounded up for display. */
object DeadlineClock {
    fun remainingSeconds(deadlineMillis: Long, nowMillis: Long): Int {
        val remaining = (deadlineMillis - nowMillis).coerceAtLeast(0L)
        return ((remaining + 999L) / 1000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}
