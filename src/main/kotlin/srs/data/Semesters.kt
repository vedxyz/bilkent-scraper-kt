package srs.data

import core.HttpUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

internal fun parseSemesters(dom: Document): List<Semester> =
    dom.select("#mainTable > tbody > tr > td > table > caption:first-child").map { caption ->
        caption.html().split("<br>").take(2).joinToString(" ").let { Semester.from(it) }
            ?: throw Exception("Failed to locate semesters while parsing")
    }

internal suspend fun getSemesters(cookie: String): List<Semester> {
    val responseText = HttpUtils.guardedFetch("https://stars.bilkent.edu.tr/srs/ajax/ladderSummary.php", cookie)
    return parseSemesters(Jsoup.parse(responseText))
}

internal fun parseCurrentSemester(dom: Document): Semester =
    dom.select("div.home-semester > span:first-child > b").text().let { Semester.from(it)!! }

internal suspend fun getCurrentSemester(cookie: String): Semester {
    val responseText = HttpUtils.guardedFetch("https://stars.bilkent.edu.tr/srs/ajax/home.php", cookie)
    return parseCurrentSemester(Jsoup.parse(responseText))
}
