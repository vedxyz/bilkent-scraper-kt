package srs.data

import core.HttpUtils
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime = LocalDateTime.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }
}

@Serializable
data class Exam(
    val courseName: String,
    val examType: String,
    @Serializable(with = LocalDateTimeSerializer::class) val startingTime: LocalDateTime,
    val timeBlock: String,
    val classrooms: List<String>?
)

internal fun parseExams(dom: Document) = dom.select("body > div").map { box ->
    val dateText = box.selectFirst(".examTable > tbody > tr:nth-child(2) > td:nth-child(2)")?.text()?.trim()
        ?: throw Exception("Couldn't find exam date")
    val timeText = box.selectFirst(".examTable > tbody > tr:nth-child(3) > td:nth-child(2)")?.text()?.trim()
        ?: throw Exception("Couldn't find exam time")

    val dateTime = try {
        LocalDateTime.parse("$dateText $timeText", DateTimeFormatter.ofPattern("dd.MM.yyyy' 'HH:mm"))
    } catch (e: DateTimeParseException) {
        throw Exception("Couldn't parse exam time")
    }

    Exam(
        courseName = box.child(1).selectFirst("h2")?.text()?.trim() ?: "N/A",
        examType = box.child(1).selectFirst("h3")?.text()?.trim() ?: "N/A",
        startingTime = dateTime,
        timeBlock = box.selectFirst(".examTable > tbody > tr:nth-child(4) > td:nth-child(2)")?.text()?.trim() ?: "N/A",
        classrooms = box.selectFirst("div[id^=examClassroomList]")!!.text().trim().split(" ")
    )
}

internal suspend fun getExams(cookie: String): List<Exam> {
    val responseText = HttpUtils.guardedFetch("https://stars.bilkent.edu.tr/srs/ajax/exam/index.php", cookie)
    return parseExams(Jsoup.parse(responseText))
}
