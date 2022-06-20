package srs.data

import core.HttpUtils
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Serializable
data class AttendanceItem(val title: String, val date: String, val attendance: String)

@Serializable
data class CourseAttendance(val title: String, val data: List<AttendanceItem>, val ratio: String)

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

internal suspend fun getAttendance(cookie: String, semester: Semester?, course: CourseSection?): List<CourseAttendance> {
    val semesterQuery: String = if (semester == null) "" else "semester=${semester.year}${semester.season.value}"
    val courseQuery: String =
        if (course == null) "" else "&course=${course.department} ${course.number}-${course.section}"

    val responseText = HttpUtils.guardedFetch(
        "https://stars.bilkent.edu.tr/srs/ajax/gradeAndAttend/attend.php?$semesterQuery$courseQuery", cookie
    )
    return parseAttendance(Jsoup.parse(responseText))
}
