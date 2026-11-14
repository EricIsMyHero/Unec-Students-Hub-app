package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DeadlineClockTest {
    @Test fun displaysTimeRoundedUpAndNeverNegative() {
        assertEquals(60, DeadlineClock.remainingSeconds(61_000, 1_000))
        assertEquals(1, DeadlineClock.remainingSeconds(61_000, 60_999))
        assertEquals(0, DeadlineClock.remainingSeconds(61_000, 61_000))
        assertEquals(0, DeadlineClock.remainingSeconds(61_000, 80_000))
    }

    @Test fun aPausedSchedulerDoesNotExtendTheDeadline() {
        val end = 1_200_000L
        assertEquals(1_200, DeadlineClock.remainingSeconds(end, 0L))
        assertEquals(100, DeadlineClock.remainingSeconds(end, 1_100_000L))
        assertEquals(0, DeadlineClock.remainingSeconds(end, 1_500_000L))
    }
}
