import data.Course
import data.CourseSection
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
    fun getAttendance() = runBlocking {
        val attendances = session.getAttendance(Semester("2021", SemesterType.Spring))
        println(jsonFmt.encodeToString(attendances))
    }

    @Test
    fun getCurriculum() = runBlocking {
        val curriculum = session.getCurriculum()
        println(jsonFmt.encodeToString(curriculum))
    }

    @Test
    fun getExams() = runBlocking {
        val exams = session.getExams()
        println(jsonFmt.encodeToString(exams))
    }

    @Test
    fun getGrades() = runBlocking {
        val grades = session.getGrades(Semester("2021", SemesterType.Spring), CourseSection("CS", "319", "1"))
        println(jsonFmt.encodeToString(grades))
    }

    @Test
    fun getLetterGradeStatistics() = runBlocking {
        val image = session.getLetterGradeStatistics(Semester("2021", SemesterType.Spring), Course("CS", "202"))
        println(image)
    }

    @Test
    fun getWeeklySchedule() = runBlocking {
        val schedule = session.getWeeklySchedule()
        println(jsonFmt.encodeToString(schedule))
    }
    @Test
    fun getSemesterCourses() = runBlocking {
        val semester = session.getSemesterCourses(Semester("2021", SemesterType.Spring))
        println(jsonFmt.encodeToString(semester))
    }

}
