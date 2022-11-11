package srs.data

import core.HttpUtils
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Represents a row on the attendance table for a course.
 */
@Serializable
data class AttendanceItem(val title: String, val date: String, val attendance: String) : java.io.Serializable

/**
 * Represents the full attendance data for a course.
 */
@Serializable
data class CourseAttendance(val title: String, val data: List<AttendanceItem>, val ratio: String) : java.io.Serializable

internal fun parseAttendance(dom: Document): List<CourseAttendance> = dom.getElementsByClass("attendDiv").map { div ->
    CourseAttendance(
        title = div.child(0).text().trim().substringAfter("Attendance Records for "),
        data = div.select("tbody > tr").drop(1).map { row ->
            AttendanceItem(
                title = row.child(0).text().trim(),
                date = row.child(1).text().trim(),
                attendance = row.child(2).text().trim()
            )
        },
        ratio = div.selectFirst("div > table + div")?.text()?.trim()?.substringAfter("Attendance Ratio: ") ?: "N/A"
    )
}

// TODO: Handle `null` semester with non-null course
internal suspend fun getAttendance(cookie: String, semester: Semester?, course: CourseSection?): List<CourseAttendance> {
    val semesterQuery: String = if (semester == null) "" else "semester=${semester.year}${semester.season.value}"
    val courseQuery: String =
        if (course == null) "" else "&course=${course.department} ${course.number}-${course.section}"

    val responseText = HttpUtils.guardedFetch(
        "https://stars.bilkent.edu.tr/srs/ajax/gradeAndAttend/attend.php?$semesterQuery$courseQuery", cookie
    )
    return parseAttendance(Jsoup.parse(responseText))
}
