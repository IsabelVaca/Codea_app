package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeOfDayTest {

    @Test
    fun formatTimeOfDay_twoDigits() {
        assertEquals("07:52", formatTimeOfDay(472))
        assertEquals("00:00", formatTimeOfDay(0))
    }

    @Test
    fun formatDuration_minutesHoursAndBoth() {
        assertEquals("0 min", formatDuration(0))
        assertEquals("45 min", formatDuration(45))
        assertEquals("1 h", formatDuration(60))
        assertEquals("16 h 46 min", formatDuration(1006))
        assertEquals("0 min", formatDuration(-5))
    }
}
