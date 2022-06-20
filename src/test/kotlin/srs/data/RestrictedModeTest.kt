package srs.data

import TestClass
import json
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll
import java.io.FileNotFoundException
import kotlin.test.assertTrue

internal class RestrictedModeTest : TestClass() {
    companion object {
        private lateinit var restrictedSample: String
        private lateinit var announcementSample: String

        @BeforeAll
        @JvmStatic
        fun setUp() = runBlocking {
            restrictedSample = readSample("/srs/restrictedMode.html")
            announcementSample = readSample("/srs/letterGradeAnnouncement.html")
        }
    }

    @Test
    fun parseSRSRestriction() {
        val restricted = parseSRSRestriction(Jsoup.parse(restrictedSample))
        assertTrue(restricted)
    }

    @Test
    fun parseLetterGrades() {
        val letterGrades = parseLetterGrades(Jsoup.parse(announcementSample))
        println(json(letterGrades))
    }
}
