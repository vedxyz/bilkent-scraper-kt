import authentication.getVerificationCode
import authentication.initializeLogin
import authentication.verifyEmail
import data.*

data class ManualVerificationIntermediary(
    val reference: String, val verify: suspend (verificationCode: String) -> SRSSession
)

class SRSSession(private val cookie: String) {
    companion object {
        @JvmStatic
        suspend fun withManualVerification(id: String, password: String): ManualVerificationIntermediary {
            val loginRequestInfo = initializeLogin(id, password)
            return ManualVerificationIntermediary(
                loginRequestInfo.reference
            ) { SRSSession(verifyEmail(loginRequestInfo.cookie, it)) }
        }

        @JvmStatic
        suspend fun withAutomatedVerification(
            id: String, password: String, email: String, emailPassword: String, boxName: String = "STARS Auth"
        ): SRSSession {
            val loginRequestInfo = initializeLogin(id, password)
            val verificationCode = getVerificationCode(email, emailPassword, boxName)
            if (loginRequestInfo.reference != verificationCode.ref) throw Exception("Reference code mismatch during automated verification (${loginRequestInfo.reference} != ${verificationCode.ref})")
            return SRSSession(verifyEmail(loginRequestInfo.cookie, verificationCode.code))
        }
    }

    suspend fun getAttendance(semester: Semester? = null, course: CourseSection? = null) =
        getAttendance(cookie, semester, course)

    suspend fun calculateGPA(courseData: List<CGPACalculationRequestData>) = calculateGPA(cookie, courseData)

    suspend fun getCurriculum() = getCurriculum(cookie)

    suspend fun getExams() = getExams(cookie)

    suspend fun getGrades(semester: Semester? = null, course: CourseSection? = null) =
        getGrades(cookie, semester, course)

    suspend fun getLetterGradeStatistics(semester: Semester, course: Course) =
        getLetterGradeStatistics(cookie, semester, course)

    suspend fun getWeeklySchedule() = getWeeklySchedule(cookie)
}
