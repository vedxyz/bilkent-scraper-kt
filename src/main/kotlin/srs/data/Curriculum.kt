package srs.data

import core.HttpUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.regex.Pattern

@Serializable
data class CurriculumCourseReplacement(val course: Course, val name: String)

@Serializable
data class CurriculumCoursePastTake(val grade: LetterGrade, val credits: String, val semester: Semester)

/**
 * Represents a single row on the curriculum.
 */
@Serializable
data class CurriculumCourseItem(
    val course: Course?,
    val name: String,
    val status: String,
    val grade: LetterGrade?,
    val credits: String?,
    var semester: Semester?,
    val replacement: CurriculumCourseReplacement?
) {
    @SerialName("pastTakes")
    private var _pastTakes: MutableList<CurriculumCoursePastTake>? = null
    val pastTakes: List<CurriculumCoursePastTake>? get() = _pastTakes?.toList()

    internal fun addPastTake(pastTake: CurriculumCoursePastTake) {
        if (_pastTakes == null) _pastTakes = mutableListOf()
        _pastTakes!!.add(pastTake)
    }
}

/**
 * Represents a full semester on the curriculum.
 */
@Serializable
data class CurriculumSemester(val items: List<CurriculumCourseItem>)

/**
 * Represents the full curriculum.
 */
@Serializable
data class Curriculum(val semesters: List<CurriculumSemester>)

private val replacementPattern = Pattern.compile("([A-Z]+)\\W(\\d+)\\W(.*)\\W?")

internal fun parseCurriculum(dom: Document) = dom.select("table.printMod").drop(2).dropLast(2).map { table ->
    table.select("tbody > tr").drop(1).map { row ->
        val course = row.child(0).text().trim().split(" ")

        CurriculumCourseItem(course = if (course.size != 2) null else Course(course[0], course[1]),
            name = row.child(1).text().trim().ifEmpty { "N/A" },
            status = row.child(2).text().trim(),
            grade = row.child(3).text().trim().let { LetterGrade.from(it.ifEmpty { "Unknown" }) },
            credits = row.child(4).text().trim().ifEmpty { "N/A" },
            semester = row.child(5).text().let { Semester.from(it) },
            replacement = row.child(6).text().trim().let { replacementPattern.matcher(it) }.run {
                if (find()) CurriculumCourseReplacement(
                    Course(
                        department = group(1), number = group(2)
                    ), name = group(3)
                )
                else null
            })
    }.let {
        val resultList = mutableListOf<CurriculumCourseItem>()
        it.forEach { item ->
            if (item.course == null && item.name == "N/A" && item.replacement == null) {
                assert(item.grade != null && item.credits != null && item.semester != null)
                resultList.last().addPastTake(CurriculumCoursePastTake(item.grade!!, item.credits!!, item.semester!!))
            } else {
                resultList.add(item)
            }
        }
        resultList
    }.let { CurriculumSemester(it) }
}.let { Curriculum(it) }

internal suspend fun getCurriculum(cookie: String): Curriculum {
    val responseText = HttpUtils.guardedFetch("https://stars.bilkent.edu.tr/srs/ajax/curriculum.php", cookie)
    return parseCurriculum(Jsoup.parse(responseText))
}
