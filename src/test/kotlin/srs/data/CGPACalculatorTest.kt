package srs.data

import TestClass
import json
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll
import java.io.FileNotFoundException

internal class CGPACalculatorTest : TestClass() {
    companion object {
        private lateinit var sample: String

        @BeforeAll
        @JvmStatic
        fun setUp() = runBlocking {
            sample = readSample("/srs/cgpa-calculator.html")
        }
    }

    @Test
    fun parseCalculation() {
        val calculation = parseCalculation(Jsoup.parse(sample))
        println(json(calculation))
    }
}
