import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class PublicBilkentProviderTest {

    @Test
    fun getAcademicCalendar(): Unit = runBlocking {
        val academicCalendar = PublicBilkentProvider.getAcademicCalendar()
        for ((date, event, type) in academicCalendar.items) {
            println("Date: '$date', Event: '$event', Type: '$type'")
            assertFalse(date.isEmpty())
            assertFalse(event.isEmpty())
        }
    }
}
