package data

import OkHttpSingleton
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Serializable
data class SemesterCourseItem(
    val course: CourseSection, val name: String, val instructor: String, val credits: String, val type: String
)

@Serializable
data class SemesterCourses(val semester: Semester, val courses: List<SemesterCourseItem>)

internal fun parseSemester(dom: Document): SemesterCourses {
    val tableId = if (dom.getElementById("coursesMenu") != null) "#coursesMenu" else "#backSemesterCourses"

    val semester = dom.selectFirst("$tableId > caption:first-child")?.text()?.let { Semester.from(it) }
        ?: throw Exception("No semester text matched")

    val courses = dom.select("$tableId > tbody > tr").map { row ->
        SemesterCourseItem(course = row.child(0)
            .text()
            .trim()
            .split(" ", "-")
            .let { (department, number, section) -> CourseSection(department, number, section) },
            name = row.child(1).text().trim(),
            instructor = row.child(2).text().trim(),
            credits = row.child(3).text().trim().ifEmpty { "-" },
            type = row.child(5).text().trim().ifEmpty { "N/A" })
    }

    return SemesterCourses(semester, courses)
}

internal suspend fun getCurrentSemesterCourses(cookie: String): SemesterCourses {
    val responseText = OkHttpSingleton.guardedFetch("https://stars.bilkent.edu.tr/srs/ajax/courses.php", cookie)
    return parseSemester(Jsoup.parse(responseText))
}

internal suspend fun getSemesterCourses(cookie: String, semester: Semester): SemesterCourses {
    val responseText = OkHttpSingleton.guardedFetch(
        "https://stars.bilkent.edu.tr/srs/ajax/semester.info.php?semester=${semester.year}${semester.season.value}",
        cookie
    )
    return parseSemester(Jsoup.parse(responseText))
}
