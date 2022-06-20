package srs.data

import core.HttpUtils
import kotlinx.serialization.Serializable
import okhttp3.FormBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Serializable
data class CGPACalculationTotals(val semesterTotal: Double, val previousTotal: Double, val grandTotal: Double)

@Serializable
data class CGPACalculation(
    val gpa: Double,
    val cgpa: Double,
    val standing: String,
    val credits: CGPACalculationTotals,
    val points: CGPACalculationTotals
)

@Serializable
data class CGPACalculationRequestData(val course: CourseSection, val grade: LetterGrade)

internal fun parseCalculation(dom: Document): CGPACalculation {
    val data =
        dom.select("div.container > div.row div.panel.panel-default > div.panel-body > table.table.table-hover.table-striped > tbody > tr > td")
            .map {
                it.text()
            }

    return CGPACalculation(
        data[0].toDouble(),
        data[1].toDouble(),
        data[2],
        CGPACalculationTotals(data[3].toDouble(), data[4].toDouble(), data[5].toDouble()),
        CGPACalculationTotals(data[6].toDouble(), data[7].toDouble(), data[8].toDouble())
    )
}

internal suspend fun calculateGPA(cookie: String, courseData: List<CGPACalculationRequestData>): CGPACalculation {
    val formBody = FormBody.Builder()
    courseData.forEach {
        formBody.add(
            "courses[${it.course.department}_${it.course.number}_${it.course.section}]", it.grade.representation
        )
    }

    val responseText = HttpUtils.guardedFetch(
        "https://stars.bilkent.edu.tr/srs-v2/tools/cgpa-calculator", cookie, formBody.build()
    )
    return parseCalculation(Jsoup.parse(responseText))
}
