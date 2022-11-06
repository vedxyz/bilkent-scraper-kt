package srs.data

import core.HttpUtils
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Serializable
data class TranscriptSemesterCourse(val course: Course, val name: String, val grade: LetterGrade, val credits: String)

/**
 * Represents a single semester on the transcript.
 */
@Serializable
data class TranscriptSemester(
    val semester: Semester,
    val gpa: String,
    val cgpa: String,
    val standing: String,
    val courses: List<TranscriptSemesterCourse>
)

@Serializable
data class Transcript(val semesters: List<TranscriptSemester>)

internal fun parseTranscript(dom: Document) = dom.select("table.printMod").drop(3).map { table ->
    val rows = table.child(0).children()
    val gpaTableRows = rows.dropLast(2).last().select("td:first-child table:first-child > tbody > tr")

    TranscriptSemester(semester = rows[0].text().let { Semester.from(it) }
        ?: throw Exception("Couldn't parse semester text"),
        gpa = gpaTableRows[0].children().last()?.text()?.trim() ?: "N/A",
        cgpa = gpaTableRows[1].children().last()?.text()?.trim() ?: "N/A",
        standing = gpaTableRows[2].children().last()?.text()?.trim() ?: "N/A",
        courses = rows.drop(2).dropLast(3).map { row ->
            TranscriptSemesterCourse(course = row.child(0)
                .text()
                .trim()
                .split(" ")
                .let { (department, number) -> Course(department, number) },
                name = row.child(1).text().trim().ifEmpty { "N/A" },
                grade = row.child(2).text().trim().let { LetterGrade.from(it) },
                credits = row.child(3).text().trim().ifEmpty { "N/A" })
        })
}.let { Transcript(it) }

internal suspend fun getTranscript(cookie: String): Transcript {
    val responseText = HttpUtils.guardedFetch("https://stars.bilkent.edu.tr/srs/ajax/transcript.php", cookie)
    return parseTranscript(Jsoup.parse(responseText))
}
