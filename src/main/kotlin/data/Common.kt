package data

import kotlinx.serialization.Serializable

@Serializable
data class Course(val department: String, val number: String)

@Serializable
data class CourseSection(val department: String, val number: String, val section: String)

@Serializable
enum class SemesterType(val value: Int) { Fall(1), Spring(2), Summer(3) }

@Serializable
data class Semester(val year: String, val season: SemesterType)

enum class LetterGrade(val representation: String) {
    A("A"),
    AMinus("A-"),
    BPlus("B+"),
    B("B"),
    BMinus("B-"),
    CPlus("C+"),
    C("C"),
    CMinus("C-"),
    DPlus("D+"),
    D("D"),
    F("F"),
    Unknown("Unknown");

    companion object {
        private val map = values().associateBy(LetterGrade::representation)
        @JvmStatic
        fun from(representation: String) = map[representation] ?: Unknown
    }
}
