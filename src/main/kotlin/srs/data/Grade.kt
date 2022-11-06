package srs.data

import core.HttpUtils
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Represents a row on the grade table for a course.
 */
@Serializable
data class GradeItem(val title: String, val date: String, val grade: String, val comment: String)

/**
 * Represents a category of grades for a course.
 */
@Serializable
data class GradeCategory(val type: String, val items: MutableList<GradeItem>)

/**
 * Represents the full grade data for a course.
 */
@Serializable
data class CourseGrades(val title: String, val categories: List<GradeCategory>)

internal fun parseGrades(dom: Document) = dom.getElementsByClass("gradeDiv").map { div ->
    val categoryTable: MutableMap<String, GradeCategory> = mutableMapOf()

    div.select("tbody > tr").drop(1).filter { it.childrenSize() == 5 }.forEach { row ->
        val categoryType = row.child(1).text().trim().ifEmpty { "Unknown" }
        categoryTable.putIfAbsent(categoryType, GradeCategory(categoryType, mutableListOf()))

        categoryTable[categoryType]!!.items.add(
            GradeItem(
                row.child(0).text().trim().ifEmpty { "N/A" },
                row.child(2).text().trim().ifEmpty { "N/A" },
                row.child(3).text().trim().ifEmpty { "N/A" },
                row.child(4).text().trim()
            )
        )
    }

    CourseGrades(
        div.child(0).text().trim().substringAfter("Grade Records for ").ifEmpty { "N/A" }, categoryTable.values.toList()
    )
}

// TODO: Handle `null` semester with non-null course
internal suspend fun getGrades(cookie: String, semester: Semester?, course: CourseSection?): List<CourseGrades> {
    if (semester == null && course != null) throw Exception("Cannot query a course with no semester provided")

    val semesterQuery: String = if (semester == null) "" else "semester=${semester.year}${semester.season.value}"
    val courseQuery: String =
        if (course == null) "" else "&course=${course.department} ${course.number}-${course.section}"

    val responseText = HttpUtils.guardedFetch(
        "https://stars.bilkent.edu.tr/srs/ajax/gradeAndAttend/grade.php?$semesterQuery$courseQuery", cookie
    )
    return parseGrades(Jsoup.parse(responseText))
}
