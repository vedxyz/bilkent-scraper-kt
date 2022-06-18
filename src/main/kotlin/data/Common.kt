package data

data class Course(val department: String, val number: String)
data class CourseSection(val department: String, val number: String, val section: String)

enum class SemesterType(val value: Int) { Fall(1), Spring(2), Summer(3) }
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
    F("F")
}
