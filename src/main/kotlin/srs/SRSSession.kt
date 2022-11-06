package srs

import srs.data.*
import srs.authentication.*

data class ManualVerificationIntermediary(
    val reference: String, val verify: suspend (verificationCode: String) -> SRSSession
)

class SRSSession(private val cookie: String) {
    companion object {
        /**
         * Creates an instance of [SRSSession] with manual verification.
         * Requires verifying the login with the returned intermediary to obtain the session.
         *
         * @return A [ManualVerificationIntermediary] to verify the login
         */
        @JvmStatic
        suspend fun withManualVerification(id: String, password: String): ManualVerificationIntermediary {
            val loginRequestInfo = initializeLogin(id, password)
            return ManualVerificationIntermediary(
                loginRequestInfo.reference
            ) { SRSSession(verifyEmail(loginRequestInfo.cookie, it)) }
        }

        /**
         * Creates an instance of [SRSSession] with automated verification, through [getVerificationCode].
         */
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

    /**
     * Provides the contents of [/srs/ajax/gradeAndAttend/attend.php](https://stars.bilkent.edu.tr/srs/ajax/gradeAndAttend/attend.php)
     *
     * This endpoint returns all data for the current semester if all parameters are omitted.
     * [semester] given alone returns all data points for that semester.
     * A [course] can be added to filter a semester.
     * Passing a [course] with a `null` [semester] filters the current semester.
     *
     * @param semester The semester to query (defaults to current semester)
     * @param course A course to filter output for (defaults to all courses)
     * @return The parsed attendance object
     */
    suspend fun getAttendance(semester: Semester? = null, course: CourseSection? = null) =
        getAttendance(cookie, semester, course)

    /**
     * Provides the functionality of [/srs-v2/tools/cgpa-calculator](https://stars.bilkent.edu.tr/srs-v2/tools/cgpa-calculator)
     *
     * @param courseData The list of courses and grades for which to calculate
     * @return The parsed calculation object
     */
    suspend fun calculateGPA(courseData: List<CGPACalculationRequestData>) = calculateGPA(cookie, courseData)

    /**
     * Provides the contents of [/srs/ajax/curriculum.php](https://stars.bilkent.edu.tr/srs/ajax/curriculum.php)
     *
     * The two information tables at the bottom are omitted.
     *
     * @return The parsed curriculum object
     */
    suspend fun getCurriculum() = getCurriculum(cookie)

    /**
     * Provides the contents of [/srs/ajax/exam/index.php](https://stars.bilkent.edu.tr/srs/ajax/exam/index.php)
     *
     * @return The parsed exam schedule object
     */
    suspend fun getExams() = getExams(cookie)

    /**
     * Provides the contents of [/srs/ajax/gradeAndAttend/grade.php](https://stars.bilkent.edu.tr/srs/ajax/gradeAndAttend/grade.php)
     *
     * This endpoint returns all data for the current semester if all parameters are omitted.
     * [semester] given alone returns all data points for that semester.
     * A [course] can be added to filter a semester.
     * Passing a [course] with a `null` [semester] filters the current semester.
     *
     * @param semester The semester to query (defaults to current semester)
     * @param course A course to filter output for (defaults to all courses)
     * @return The parsed grades object
     */
    suspend fun getGrades(semester: Semester? = null, course: CourseSection? = null) =
        getGrades(cookie, semester, course)

    /**
     * Provides the contents of [/srs/ajax/infoCard.php](https://stars.bilkent.edu.tr/srs/ajax/infoCard.php)
     *
     * Some arguably less important categories are omitted.
     *
     * @return The parsed information card object
     */
    suspend fun getInformationCard() = getInformationCard(cookie)

    /**
     * Provides the contents of [/srs/ajax/stats/letter-grade-bar.php](https://stars.bilkent.edu.tr/srs/ajax/stats/letter-grade-bar.php)
     *
     * @param semester The semester for which to query statistics
     * @param course The course for which to retrieve statistics
     * @return The statistics image, encoded as a base64 string
     */
    suspend fun getLetterGradeStatistics(semester: Semester, course: Course) =
        getLetterGradeStatistics(cookie, semester, course)

    /**
     * Provides the contents of [/srs-v2/schedule/index/weekly](https://stars.bilkent.edu.tr/srs-v2/schedule/index/weekly)
     *
     * The full matrix of time slots is parsed, meaning evening-hours and weekends are included.
     *
     * @return The parsed weekly schedule object
     */
    suspend fun getWeeklySchedule() = getWeeklySchedule(cookie)

    /**
     * Provides the contents of either [/srs/ajax/courses.php](https://stars.bilkent.edu.tr/srs/ajax/courses.php)
     * or [/srs/ajax/semester.info.php](https://stars.bilkent.edu.tr/srs/ajax/semester.info.php)
     *
     * If the [semester] parameter is left `null`, the current semester is retrieved.
     *
     * @param semester The semester to retrieve information for
     * @return The parsed semester object
     */
    suspend fun getSemesterCourses(semester: Semester? = null) =
        if (semester == null) getCurrentSemesterCourses(cookie) else getSemesterCourses(cookie, semester)

    /**
     * Provides the contents of [/srs/ajax/transcript.php](https://stars.bilkent.edu.tr/srs/ajax/transcript.php)
     *
     * The credit and point sums are omitted.
     *
     * @return The parsed transcript object
     */
    suspend fun getTranscript() = getTranscript(cookie)
}
