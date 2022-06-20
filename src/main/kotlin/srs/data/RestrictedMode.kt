package srs.data

import core.HttpMethods
import core.HttpUtils
import kotlinx.serialization.Serializable
import okhttp3.FormBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Serializable
data class LetterGradeResult(val course: String, val grade: LetterGrade)

internal fun parseSRSRestriction(dom: Document) =
    dom.selectFirst("#wrapper > div.info")?.text()?.contains("SRS is in restricted mode") ?: false

internal fun parseLetterGrades(dom: Document) = dom.select("#wrapper > fieldset > table > tbody > tr").map { row ->
    LetterGradeResult(row.child(0).text().trim(), row.child(1).text().let { LetterGrade.from(it) })
}

internal suspend fun isSRSRestricted(): Boolean {
    HttpUtils.fetch("https://stars.bilkent.edu.tr/srs/").use {
        return parseSRSRestriction(Jsoup.parse(it.body!!.string()))
    }
}

internal suspend fun getLetterGrades(id: String, password: String): List<LetterGradeResult> {
    HttpUtils.fetch(
        "https://stars.bilkent.edu.tr/srs/index.php",
        HttpMethods.GET,
        null,
        FormBody.Builder().add("user_id", id).add("password", password).build()
    ).use {
        return parseLetterGrades(Jsoup.parse(it.body!!.string()))
    }
}
