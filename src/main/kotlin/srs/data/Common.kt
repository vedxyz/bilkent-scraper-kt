package srs.data

import kotlinx.serialization.Serializable
import java.util.regex.Pattern

/**
 * @param department The department segment of the course: **CS** 101-1
 * @param number The number segment of the course: CS **101**-1
 */
@Serializable
data class Course(val department: String, val number: String)

/**
 * @param department The department segment of the course: **CS** 101-1
 * @param number The number segment of the course: CS **101**-1
 * @param section The section segment of the course: CS 101-**1**
 */
@Serializable
data class CourseSection(val department: String, val number: String, val section: String)

@Serializable
enum class SemesterType(val value: Int) { Fall(1), Spring(2), Summer(3) }

/**
 * @param year The year segment of the semester: **2020**-2021 Fall
 * @param season The season segment of the semester: 2020-2021 **Fall**
 */
@Serializable
data class Semester(val year: String, val season: SemesterType) : Comparable<Semester> {
    companion object {
        private val semesterPattern = Pattern.compile("(\\d{4})-\\d{4}\\W(Fall|Spring|Summer)")

        /**
         * @param text A semester string in the format '`2021-2022 Fall`'
         */
        @JvmStatic
        fun from(text: String): Semester? = semesterPattern.matcher(text.trim()).run {
            if (find()) Semester(group(1), SemesterType.valueOf(group(2))) else null
        }
    }

    override fun compareTo(other: Semester): Int = compareValuesBy(this, other, { it.year }, { it.season })
}

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
