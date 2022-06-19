package data

import OkHttpSingleton
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

enum class TimeSlot(val representation: String) {
    H8("08:30 - 09:20"),
    H9("09:30 - 10:20"),
    H10("10:30 - 11:20"),
    H11("11:30 - 12:20"),
    H12("12:30 - 13:20"),
    H13("13:30 - 14:20"),
    H14("14:30 - 15:20"),
    H15("15:30 - 16:20"),
    H16("16:30 - 17:20"),
    H17("17:30 - 18:20"),
    H18("18:30 - 19:20"),
    H19("19:30 - 20:20"),
    H20("20:30 - 21:20"),
    H21("21:30 - 22:20");

    companion object {
        private val map = values().associateBy(TimeSlot::representation)

        @JvmStatic
        fun from(representation: String) = map[representation]
            ?: throw IllegalArgumentException("No value corresponding to '$representation'")
    }
}

@Serializable
data class DailyScheduleItem(val timeSlot: TimeSlot, val details: List<String>?)

@Serializable
data class DailySchedule(val timeSlots: List<DailyScheduleItem>)

@Serializable
data class WeeklySchedule(val days: List<DailySchedule>)

internal fun parseWeeklySchedule(dom: Document): WeeklySchedule {
    val rows = dom.select("body > div.container > div.row div.row + table > tbody > tr").drop(1)
    val days = mutableListOf<DailySchedule>()

    for (i in 1..7) days.add(rows.map { row ->
        DailyScheduleItem(
            timeSlot = row.child(0).text().trim().let { TimeSlot.from(it) },
            details = if (row.child(i).childrenSize() == 0) null else row.child(i)
                .child(0)
                .html()
                .dropLast("<br>".length)
                .split("<br>")
        )
    }.let { DailySchedule(it) })

    return WeeklySchedule(days)
}

internal suspend fun getWeeklySchedule(cookie: String): WeeklySchedule {
    val responseText = OkHttpSingleton.guardedFetch("https://stars.bilkent.edu.tr/srs-v2/schedule/index/weekly", cookie)
    return parseWeeklySchedule(Jsoup.parse(responseText))
}
