package srs.data

import TestClass
import json
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll
import java.io.FileNotFoundException

internal class AttendanceTest : TestClass() {
    companion object {
        private lateinit var sample: String

        @BeforeAll
        @JvmStatic
        fun setUp() = runBlocking {
            sample = readSample("/srs/attend.html")
        }
    }

    @Test
    fun parseAttendance() {
        val attendance = parseAttendance(Jsoup.parse(sample))
        println(json(attendance))
    }
}
