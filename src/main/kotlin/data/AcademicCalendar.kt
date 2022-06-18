package data

import OkHttpSingleton
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

enum class AcademicCalendarEventType { StudentAffairs, Vacation, EnglishPrep }

@Serializable
data class AcademicCalendarItem(val date: String, val event: String, val type: AcademicCalendarEventType?)

@Serializable
data class AcademicCalendar(val items: List<AcademicCalendarItem>)

private fun getEventType(td: Element?): AcademicCalendarEventType? =
    if (td == null || td.childrenSize() == 0) null else when (td.child(0).className()) {
        "oim" -> AcademicCalendarEventType.StudentAffairs
        "tatil" -> AcademicCalendarEventType.Vacation
        "idmyo" -> AcademicCalendarEventType.EnglishPrep
        else -> null
    }

internal fun parseAcademicCalendar(dom: Document): AcademicCalendar =
    AcademicCalendar(dom.select("tbody > tr").map { row ->
        AcademicCalendarItem(date = row.child(0).text().trim().filterNot { it == '\n' } ?: "N/A",
            event = row.child(1).text().trim() ?: "N/A",
            type = getEventType(row.child(1)))
    })

internal suspend fun getAcademicCalendar(): AcademicCalendar {
    OkHttpSingleton.fetch("https://w3.bilkent.edu.tr/bilkent/academic-calendar/").use {
        return parseAcademicCalendar(Jsoup.parse(it.body!!.string()))
    }
}
