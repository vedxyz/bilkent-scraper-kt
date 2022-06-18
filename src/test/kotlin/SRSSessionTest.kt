import data.Course
import data.Semester
import data.SemesterType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class SRSSessionTest {
    companion object {
        private lateinit var session: SRSSession

        @BeforeAll
        @JvmStatic
        fun setUp() = runBlocking {
            session = SRSSession.withAutomatedVerification(srsId, srsPassword, webmailAddress, webmailPassword)
        }
    }

    private val jsonFmt = Json { prettyPrint = true }

    @Test
    fun getAttendanceTest() = runBlocking {
        val attendances = session.getAttendance(Semester("2021", SemesterType.Spring))
        println(jsonFmt.encodeToString(attendances))
    }

    @Test
    fun getLetterGradeStatistics() = runBlocking {
        val image = session.getLetterGradeStatistics(Semester("2021", SemesterType.Spring), Course("CS", "202"))
        println(image)
    }
}
