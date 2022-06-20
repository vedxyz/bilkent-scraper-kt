package srs.data

import core.HttpUtils
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@Serializable
data class StudentInformation(
    val id: String,
    val nationalId: String,
    val fullName: String,
    val status: String,
    val faculty: String,
    val department: String,
    val picture: String?
)

@Serializable
data class AdvisorInformation(val fullName: String, val email: String)

@Serializable
data class CourseLimits(val lower: String, val upper: String)

@Serializable
data class Ranking(val cohort: String, val agpa: String, val details: String)

@Serializable
data class AcademicInformation(
    val standing: String,
    val gpa: String,
    val cgpa: String,
    val registrationSemester: String,
    val curriculumSemester: String,
    val inClass: String,
    val nominalCreditLoad: String,
    val courseLimits: CourseLimits,
    val ranking: Ranking
)

@Serializable
data class ScholarshipInformation(val byPlacement: String, val merit: String)

@Serializable
data class ContactInformation(val contactEmail: String, val bilkentEmail: String, val mobilePhone: String)

@Serializable
data class InformationCard(
    val student: StudentInformation,
    val advisor: AdvisorInformation,
    val academic: AcademicInformation,
    val scholarship: ScholarshipInformation,
    val contact: ContactInformation
)

private fun extract(table: Element?, selector: String) =
    table?.selectFirst(selector)?.text()?.trim()?.ifEmpty { null } ?: "-"

private fun extractRow(table: Element?, index: Int) = extract(table, "tr:nth-child($index) > td")

internal fun parseInformationCard(dom: Document, picture: String?): InformationCard {
    val (studentAndAdvisorBox, academicBox, scholarshipBox, contactBox) = dom.select("#infoCardContainer > fieldset")
    val (studentTable, advisorTable) = studentAndAdvisorBox.select("tbody")
    val academicTable = academicBox.selectFirst("tbody")
    val scholarshipTable = scholarshipBox.selectFirst("tbody")
    val contactTable = contactBox.selectFirst("tbody")

    return InformationCard(
        StudentInformation(
            id = extractRow(studentTable, 1),
            nationalId = extractRow(studentTable, 2),
            fullName = extractRow(studentTable, 3),
            status = extractRow(studentTable, 4),
            faculty = extractRow(studentTable, 5),
            department = extractRow(studentTable, 6),
            picture
        ), AdvisorInformation(
            fullName = extractRow(advisorTable, 1), email = extractRow(advisorTable, 2)
        ), AcademicInformation(
            standing = extractRow(academicTable, 1),
            gpa = extractRow(academicTable, 2),
            cgpa = extractRow(academicTable, 3),
            registrationSemester = extractRow(academicTable, 5),
            curriculumSemester = extractRow(academicTable, 6),
            inClass = extractRow(academicTable, 7),
            nominalCreditLoad = extractRow(academicTable, 8),
            courseLimits = CourseLimits(lower = extractRow(academicTable, 9), upper = extractRow(academicTable, 10)),
            ranking = Ranking(
                cohort = extractRow(academicTable, 12),
                agpa = extractRow(academicTable, 13),
                details = extractRow(academicTable, 14)
            )
        ), ScholarshipInformation(
            byPlacement = extractRow(scholarshipTable, 1), merit = extractRow(scholarshipTable, 2)
        ), ContactInformation(
            contactEmail = extractRow(contactTable, 1),
            bilkentEmail = extractRow(contactTable, 2),
            mobilePhone = extractRow(contactTable, 4)
        )
    )
}

internal suspend fun getInformationCard(cookie: String): InformationCard {
    val responseText = HttpUtils.guardedFetch("https://stars.bilkent.edu.tr/srs/ajax/infoCard.php", cookie)
    val dom = Jsoup.parse(responseText)
    val picture = HttpUtils.guardedFetchImage(
        dom.selectFirst("fieldset > table > tbody > tr > td > img")?.attr("src")
            ?: "https://stars.bilkent.edu.tr/webserv/image.php", cookie
    )
    return parseInformationCard(dom, picture)
}
